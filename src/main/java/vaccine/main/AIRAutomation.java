package vaccine.main;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;
import com.codeborne.selenide.impl.JavaScript;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.Request;
import javafx.application.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.*;
import static org.openqa.selenium.Keys.ENTER;

public class AIRAutomation {


    final static int lagBehindPatients = 5;
    final static int waitTimeInMinutes = 20;
    private static final Date date = new Date();
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
    private static final String str = formatter.format(date);
    private static final ArrayList<ArrayList<String>> vaccinatedList = new ArrayList<>();
    private static final ArrayList<ArrayList<String>> warnedList = new ArrayList<>();
    public static MainController controller;
    static Spreadsheet spreadsheet;
    static boolean autoMode = false;
    static boolean connected = false;
    static boolean step = false;
    //WEB ELEMENTS
    static String vaccinateTabButton = "/html/body/div[4]/div[2]/div/div[2]/div/div[1]/div/div/ul/li[3]/a";
    static String vaccinateRecord = "//*[@title=\"Save vaccination record\"]";
    static String nextButton = "//*[@title=\"Next\"]";
    static String radioDoseOne = "//*[@class=\"slds-radio\" and position() = 1]";
    static String radioDoseTwo = "//*[@class=\"slds-radio\" and position() = 2]";
    static String patientListLoadChecker = "//*[contains(text(),'Updated')]";
    static String astrazenVaccine = "//*[contains(text(),'AstraZeneca')]";
    static String pfizerVaccine = "//*[contains(text(),'Pfizer')]";
    static String vaccineComboBox = "//*/lightning-base-combobox";
    static String searchBox = "/html/body/div[4]/div[2]/div/div/div/div/div[3]/div/div/div[2]/div/div[1]/div[2]/div[2]/force-list-view-manager-search-bar/div/lightning-input/div/input";
    static ArrayList<Integer> waitingTime = new ArrayList<>();
    static ArrayList<ArrayList<String>> waitHistory = new ArrayList<>();
    static ArrayList<ArrayList<String>> failedList = new ArrayList<>();
    static Thread autoLoop;
    private static FileWriter vaccinateOut;
    private static Integer encounterColumnIndex = null;
    private static Integer doseReadyColumnIndex = null;
    private static Integer AIRColumnIndex = null;
    private static Integer vaccinatedColumnIndex = null;
    private static int numOfColumns;
    private static Integer firstNameColumnIndex;
    private static Integer lastNameColumnIndex;
    private static Integer DOBColumnIndex;


    static {
        try {
            vaccinateOut = new FileWriter((str + "ClinicAssistant").replace('/', '-'));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AIRAutomation(){
    }


    /**
     *
     */
    private static void getInitialVaccinations() {
        FileReader vaccinateIn = null;
        try {
            vaccinateIn = new FileReader((str + "ClinicAssistant").replace('/', '-'));
            if (vaccinateIn.read() == -1)
                loadCurrentVaccinationsSpreadsheet();
            return;
        } catch (FileNotFoundException e) {
            loadCurrentVaccinationsSpreadsheet();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        String vaccinated = fileToString(vaccinateIn).replace(System.getProperty("line.separator"), "");
        String[] vaccinatedData = vaccinated.split("@$");

        for (int z = 0; z < (vaccinatedData.length); z = z + 3) {
            ArrayList<String> strings = new ArrayList<>();
            for (int c = 0; c < 20; c++)
                strings.add("");

            strings.set(firstNameColumnIndex, vaccinatedData[z]);
            System.out.println(vaccinatedData[z]);
            strings.set(lastNameColumnIndex, vaccinatedData[2 * z]);
            strings.set(DOBColumnIndex, vaccinatedData[3 * z]);
            vaccinatedList.add(strings);
        }
    }

    static String fileToString(FileReader vaccinateIn) {
        int character = 0;
        StringBuilder data = new StringBuilder();

        while (true) {
            try {
                if ((character = vaccinateIn.read()) == -1) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            data.append((char) character);
        }
        return data.toString();
    }

    /**
     *
     */
    private static void loadCurrentVaccinationsSpreadsheet() {
        for (ArrayList<String> row : spreadsheet.getCopyRows()) {
            if (row.get(AIRColumnIndex).equals("BOT")) {
                vaccinatedList.add(row);
                System.out.println(row.get(firstNameColumnIndex));
            }
        }
    }

    /**
     * Sets object controller and vaccinetype
     * @param mainController the main controller
     */
    public static void configure(MainController mainController) {
        controller = mainController;


    }

    /**
     *
     */
    public static void stop() {
        autoMode = false;
    }

    /**
     *
     */
    public static void start() {
        autoMode = true;
    }

    /**
     *
     */
    public static void verifyAll() {
        for (ArrayList<String> patient : GoogleManager.getSpreadsheets().get(0).getCopyRows())
            verify(patient);
    }

    /**
     * Verifies if the given patient has the correct initials in the AIR cell
     * @param patient patient to verify
     */
    public static void verify(ArrayList<String> patient) {
        if (!patient.get(AIRColumnIndex).equals("BOT")) {
            for (ArrayList<String> patientRows : vaccinatedList) {
                if (isEqual(patient, patientRows)) {
                    GoogleManager.getSpreadsheets().get(0).editRow(GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(patient), AIRColumnIndex, "BOT");
                }
            }
        } else {
            //if equal then
            boolean hasBeenVaccinated = false;
            for (ArrayList<String> patientRows : vaccinatedList) {
                if (isEqual(patient, patientRows)) {
                    hasBeenVaccinated = true;
                    break;
                }
            }
            if (!hasBeenVaccinated && (!alreadyWarned(patient))) {
                Color color = new Color();
                color.setGreen(0f);
                color.setRed(1f);
                color.setBlue(0f);
                ArrayList<Request> requests = new ArrayList<>();
                requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(patient), AIRColumnIndex, color));
                try {
                    GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("#################################################################");
                warnedList.add(patient);


                System.out.println("This instance has not vaccinated the following Patient");
                System.out.println(patient.get(firstNameColumnIndex) + " " + patient.get(lastNameColumnIndex));
                System.out.println("Please note: If the program was recently restarted bot has lost record of previous AIRs it has done");
                System.out.println("If you believe this to be the case, then there is no cause for concern.");

            }
        }
    }

    /**
     * @param patient the patient to check if been warned about
     * @return true if program has notified of patient
     */
    private static boolean alreadyWarned(ArrayList<String> patient) {
        for (ArrayList<String> patientRow : warnedList) {
            if (isEqual(patientRow, patient))
                return true;
        }

        return false;
    }

    /**
     *Sets up the selenium instance through the browser debugger
     */
    static void connect() {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        WebDriver driver = new ChromeDriver(options);
        WebDriverRunner.setWebDriver(driver);
        connected = true;
        open("https://vaccines.digitalhealth.gov.au/healthcare-providers/s/patients-list-");

    }

    /**
     *Starts the autoloop
     */
    static void startLoop() {
        if (autoLoop == null) {
            autoLoop = new Thread(new AutomationLoop());
            autoLoop.start();
        }

        spreadsheet = GoogleManager.getSpreadsheets().get(0);
        Configuration.timeout = 16000;

        encounterColumnIndex = spreadsheet.getColumnIndex(controller.EncounterColumnName.getText());
        doseReadyColumnIndex = spreadsheet.getColumnIndex(controller.DoseReadyColumnName.getText());
        AIRColumnIndex = spreadsheet.getColumnIndex(controller.AIRColumnName.getText());
        vaccinatedColumnIndex = spreadsheet.getColumnIndex(controller.VaccinatedColumnName.getText());

        firstNameColumnIndex = spreadsheet.getColumnIndex(controller.FirstNameColumnName.getText());
        lastNameColumnIndex = spreadsheet.getColumnIndex(controller.LastNameColumnName.getText());
        DOBColumnIndex = spreadsheet.getColumnIndex(controller.DOBColumnName.getText());
        numOfColumns = spreadsheet.getCopyRows().get(0).size();
    }


    /**
     * Returns the number of patients ready for their AIR to be done
     * @return number of patients ready for AIR
     */
    static int getReadyCount() {
        int i = 0;
        for (ArrayList<String> columnsOfPatient : spreadsheet.getCopyRows())
            if (isReady(columnsOfPatient))
                i++;


        int finalI = i;
        Platform.runLater(() -> {
            controller.readyCount.textProperty().set(String.valueOf(finalI));
            controller.failedCount.textProperty().set(String.valueOf(failedList.size()));
            controller.completedCount.textProperty().set(String.valueOf(vaccinatedList.size()));
            controller.botStatus.textProperty().set("Waiting...");

        });
        return i;
    }

    /**
     * returns a patient that is ready to have their AIR done
     * @return returns a patient that is ready
     */
    static ArrayList<String> getAReadyPatient() {
        //if no patient ready then return null
        for (ArrayList<String> columnsOfPatient : spreadsheet.getCopyRows()) {
            if (isReady(columnsOfPatient)) {
                return columnsOfPatient;
            }
        }
        return null;
    }

    /**
     * if their first name, last name and DOB match, then they are equal
     * @param person a patient
     * @param person2 a patient
     * @return true if equal
     */
    private static boolean isEqual(ArrayList<String> person, ArrayList<String> person2) {
        return person.get(firstNameColumnIndex).equals(person2.get(firstNameColumnIndex))
                && person.get(lastNameColumnIndex).equals(person2.get(lastNameColumnIndex))
                && person.get(DOBColumnIndex).equals(person2.get(DOBColumnIndex));
    }

    /**
     * Checks if a patient is ready to get their AIR done
     * @param patient a patient to check if ready
     * @return true if ready for AIR
     */
    private static boolean isReady(ArrayList<String> patient) {
        if (patient.size() != numOfColumns)
            return false;

        //if failed then ignore
        for (int i = 0; i < failedList.size(); i++)
            if (isEqual(patient, failedList.get(i)))
                return false;

        //if vaccinated then ignore
        for (int i = 0; i < vaccinatedList.size(); i++)
            if (isEqual(patient, vaccinatedList.get(i)))
                return false;

        for (int i = 0; i < waitHistory.size(); i++)
            if (isEqual(patient, waitHistory.get(i)))
                if (waitingTime.get(i) > Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE))
                    return false;

        //if not eligible to get AIR then ignore
        if (patient.get(encounterColumnIndex).equals("") || patient.get(encounterColumnIndex).toLowerCase(Locale.ROOT).equals("x"))
            return false;

        if (patient.get(vaccinatedColumnIndex).equals("") || patient.get(vaccinatedColumnIndex).toLowerCase(Locale.ROOT).equals("x"))
            return false;

        if (patient.get(doseReadyColumnIndex).equals("") || patient.get(doseReadyColumnIndex).toLowerCase(Locale.ROOT).equals("x"))
            return false;

        return (patient.get(AIRColumnIndex).equals("") || patient.get(AIRColumnIndex) == null);

    }

    /**
     *Steps the bot one patient at a time
     */
    static void stepBot() {
        step = true;
    }

    /**
     *Does a patients AIR
     */
    static void doPatient() {
        try {
            //get next available row to vaccinate
            ArrayList<String> selectedPatient = getAReadyPatient();

            Platform.runLater(() -> {
                controller.botStatus.textProperty().set("AIR-ing.");
                for (Object patient : controller.table.getItems()) {
                    Patient temp = (Patient) patient;
                    if (temp.getFirstName().equals(selectedPatient.get(firstNameColumnIndex)))
                        if (temp.getLastName().equals(selectedPatient.get(lastNameColumnIndex)))
                            if (temp.getDOB().equals(selectedPatient.get(DOBColumnIndex)))
                                controller.table.getSelectionModel().select(patient);

                }

            });

            ArrayList<Request> requests = new ArrayList<>();
            Color color = new Color();
            color.setRed(.68f);
            color.setGreen(.85f);
            color.setBlue(.90f);

            requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(selectedPatient), AIRColumnIndex, color));
            GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());


            open("https://vaccines.digitalhealth.gov.au/healthcare-providers/s/patients-list-");
            String firstName = selectedPatient.get(firstNameColumnIndex).toLowerCase(Locale.ROOT);
            String lastName = selectedPatient.get(lastNameColumnIndex).toLowerCase(Locale.ROOT);
            int dose = Integer.parseInt(selectedPatient.get(doseReadyColumnIndex));
            if (vaccinate(firstName, lastName, dose)) {
                color.setRed(0f);
                color.setGreen(1f);
                color.setBlue(0f);
                requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(selectedPatient), AIRColumnIndex, color));
                GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());
                //dataHandler.setCell("AIR", "BOT", ColorProperty.getAsGreen().getGoogleColor(), selectedPatient);
                spreadsheet.editRow(spreadsheet.getCopyRows().indexOf(selectedPatient), AIRColumnIndex, "BOT");
                vaccinatedList.add(selectedPatient);
                vaccinateOut.write(firstName + "@$" + lastName + "@$" + selectedPatient.get(DOBColumnIndex));
                vaccinateOut.flush();
            } else if (!isWaiting(selectedPatient)) {
                    color.setRed(1f);
                    color.setGreen(1f);
                    color.setBlue(0f);

                    requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(selectedPatient), AIRColumnIndex, color));
                    GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());
                    waitHistory.add(selectedPatient);
                    int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE);
                    waitingTime.add(currentTime + waitTimeInMinutes);
                    //make Patient yellow
                    //dataHandler.setCell("AIR", ColorProperty.getAsYellow().getGoogleColor(), selectedPatient);

                } else {
                    color.setRed(1f);
                    color.setGreen(165f / 256f);
                    color.setBlue(0f);
                    requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(selectedPatient), AIRColumnIndex, color));
                    GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());

                    failedList.add(selectedPatient);
                    //dataHandler.setCell("AIR", ColorProperty.getAsOrange().getGoogleColor(), selectedPatient);


                }

        } catch (Exception a) {
            a.printStackTrace();
            System.out.println(a.getCause());


        }
    }

    /**
     * checks if a patient is waiting
     * @param selectedPatient patient to check
     * @return true if they are waiting
     */
    private static boolean isWaiting(ArrayList<String> selectedPatient) {
        for (ArrayList<String> patient : waitHistory)
            if (isEqual(selectedPatient, patient))
                return true;

        return false;
    }

    /**
     * @param firstName
     * @param lastName
     * @param dose
     * @return
     */
    static boolean vaccinate(String firstName, String lastName, int dose) {
        if (firstName.contains("'") || lastName.contains("'"))
            return false;


        //spreadsheet.setCell("AIR", new ColorProperty(.68f,.85f,.90f).getGoogleColor(), patient);
        boolean found = search(firstName, lastName);
        if (!found) {
            return false;
        }
        boolean vaccinated = apply(dose);
        return vaccinated;
    }

    /**
     * @param firstName
     * @param lastName
     * @return
     */
    private static boolean search(String firstName, String lastName) {

        if (hasNotLoadedPatientList())
            return false;

        //SEARCH BOX
        $(byXpath(searchBox)).sendKeys(firstName + " " + lastName);
        $(byXpath(searchBox)).sendKeys(ENTER);

        //PATIENT ENTRY FOR AIR
        if (!isPatientFound(firstName, lastName))
            return false;

        //PATIENT LINK FOR AIR
        String link = $(byXpath("//a[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + firstName + "') and contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lastName + "')]")).getAttribute("href");
        open(link);

        return true;
    }

    /**
     * @param dose
     * @return
     */
    private static boolean apply(int dose) {
        //WAIT FOR VACCINATE TAB TO SHOW
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            $(byXpath("//*[contains(text(),'Do not change')]")).should(exist);
        } catch (com.codeborne.selenide.ex.ElementNotFound e) {
            return false;
        }
        sleep(1000);

        $(byXpath(vaccinateTabButton)).click();


        //WAIT FOR COMBO BOX TO BE READY
        try {
            $(byXpath(vaccineComboBox)).shouldBe(enabled);
        } catch (com.codeborne.selenide.ex.ElementNotFound e) {
            return false;
        }

        //CLICK VACCINE COMBOBOX
        new JavaScript(executeJavaScript("window.scrollTo(0, document.body.scrollHeight)"));

        $(byXpath(vaccineComboBox)).click();


        //SET VACCINE
        if (controller.vaccineSelection.getText().toLowerCase(Locale.ROOT).contains("as")) {
            //select Astrazeneca
            $(byXpath(astrazenVaccine)).click();
        } else{
            //select pfizer
            $(byXpath(pfizerVaccine)).click();
        }

        //SET DOSE
        if (dose == 1) {
            //select first dose
            $(byXpath(radioDoseOne)).click();
        } else if (dose == 2) {
            //select 2nd dose
            $(byXpath(radioDoseTwo)).click();

        } else {
            return false;
        }

        //NEXT BUTTON
        $(byXpath(nextButton)).click();


        //SAVE VACCINE
        try {
            $(byXpath(vaccinateRecord)).should(exist);
        } catch (com.codeborne.selenide.ex.ElementNotFound e) {
            System.out.println("WARNING: AIR SAVE BUTTON MISSING");
            autoMode = false;
            Platform.runLater(() -> {
                //status.set("ERROR");


            });
            return false;
        }
        if (controller.vaccineSelection.getText().toLowerCase(Locale.ROOT).contains("az"))
            if ($(byXpath("//*[contains(text(),'Pfizer')]")).exists())
                return false;

        if (controller.vaccineSelection.getText().toLowerCase(Locale.ROOT).contains("pf"))
            if ($(byXpath("//*[contains(text(),'Astrazen')]")).exists())
                return false;


        $(byXpath(vaccinateRecord)).click();

        return true;

    }

    /**
     * @return
     */
    private static boolean createRecord() {
        boolean createRecordButtonPresent = $(byXpath("/html/body/div[4]/div[2]/div/div[2]/div/div[1]/div/div/ul/li[3]/a")).exists();
        if (!createRecordButtonPresent) {
            return false;
        }
        $(byXpath("/html/body/div[4]/div[2]/div/div[2]/div/div[1]/div/div/ul/li[3]/a")).click();
        return true;
    }

    /**
     * @param firstName
     * @param lastName
     * @return
     */
    private static boolean isPatientFound(String firstName, String lastName) {
        try {
            $(byXpath("//*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + firstName + "') and contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lastName + "')]")).should(exist);
        } catch (com.codeborne.selenide.ex.ElementNotFound e) {
            return false;
        }
        return true;
    }

    /**
     * @return
     */
    private static boolean hasNotLoadedPatientList() {
        try {
            $(byXpath(patientListLoadChecker)).should(exist);
        } catch (com.codeborne.selenide.ex.ElementNotFound E) {
            System.out.println("WARNING: Patient list not loading");
            autoMode = false;
            Platform.runLater(() -> {
                //status.set("ERROR");


            });

            return true;
        }
        return false;
    }


    /**
     *
     */
    static class AutomationLoop implements Runnable {

        private final ArrayList<ArrayList<Object>> readyList = new ArrayList<>();

        @Override
        public void run() throws com.codeborne.selenide.ex.ElementNotFound {
            connect();
            while (!Thread.currentThread().isInterrupted() && spreadsheet.getCopyRows().size() == 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    return;
                }
            }


            getInitialVaccinations();

            while (!Thread.currentThread().isInterrupted()) {


                verifyAll();
                if ((autoMode || step) && getReadyCount() > lagBehindPatients) {

                    doPatient();
                    step = false;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }


    }


}
