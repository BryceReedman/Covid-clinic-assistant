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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.codeborne.selenide.Condition.enabled;
import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Selectors.byXpath;
import static com.codeborne.selenide.Selenide.*;
import static org.openqa.selenium.Keys.ENTER;

public class AIRAutomation {


    final static int lagBehindPatients = 0;
    final static int waitTimeInMinutes = 20;
    private static final Date date = new Date();
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yy");
    private static final String str = formatter.format(date);
    private static final ArrayList<ArrayList<String>> vaccinatedList = new ArrayList<ArrayList<String>>();
    private static final ArrayList<ArrayList<String>> warnedList = new ArrayList();
    static String status = "Offline";
    static Spreadsheet spreadsheet;
    static String VaccineType;
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
    static ArrayList<ArrayList<String>> waitHistory = new ArrayList<ArrayList<String>>();
    static ArrayList<ArrayList<String>> failedList = new ArrayList<ArrayList<String>>();
    static Thread autoLoop;
    private static FileWriter vaccinateOut;
    private static Integer encounterColumnIndex = null;
    private static Integer doseReadyColumnIndex = null;
    private static Integer AIRColumnIndex = null;
    private static Integer vaccinatedColumnIndex = null;
    private static int numOfColums;
    public static MainController controller;
    private static String vaccineType;
    private static Integer firstNameColumnIndex;
    private static Integer lastNamecolumnIndex;
    private static Integer DOBColumnIndex;



    static {
        try {
            vaccinateOut = new FileWriter((str + "ClinicAssistant").replace('/', '-'));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AIRAutomation() throws IOException {
    }

    private static void getInitialVaccinations() {
        FileReader vaccinateIn = null;
        try {
            vaccinateIn = new FileReader((str + "ClinicAssistant").replace('/', '-'));
            if (vaccinateIn.read() == -1)
                loadCurrentVaccintaionsSpreadsheet();
            return;
        } catch (FileNotFoundException e) {
            loadCurrentVaccintaionsSpreadsheet();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        int character = 0;
        StringBuilder data = new StringBuilder();

        while (true) {
            try {
                if (!((character = vaccinateIn.read()) != -1)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            data.append((char) character);
        }
        String vaccinated = data.toString().replace(System.getProperty("line.separator"), "");
        String[] vaccinatedData = vaccinated.split("@$");

        for (int z = 0; z < (vaccinatedData.length); z = z + 3) {
            ArrayList<String> strings = new ArrayList<>();
            for (int c = 0; c < 20; c++)
                strings.add("");

            strings.set(firstNameColumnIndex, vaccinatedData[1 * z]);
            System.out.println(vaccinatedData[1 * z]);
            strings.set(lastNamecolumnIndex, vaccinatedData[2 * z]);
            strings.set(DOBColumnIndex, vaccinatedData[3 * z]);
            vaccinatedList.add(strings);
        }
    }

    private static void loadCurrentVaccintaionsSpreadsheet() {
        for (ArrayList<String> row : spreadsheet.getCopyRows()) {
            if (row.get(AIRColumnIndex).equals("BOT")) {
                vaccinatedList.add(row);
                System.out.println(row.get(firstNameColumnIndex));
            }
        }
    }

    public static void configure(MainController mainController) {
        controller = mainController;
        vaccineType = controller.vaccineSelection.getText();



    }

    public static void stop() {
        autoMode = false;
    }

    public static void start() {
        autoMode = true;
    }

    public static void verifyAll() {
        for (ArrayList<String> patient : GoogleManager.getSpreadsheets().get(0).getCopyRows())
            verify(patient);
    }

    public static void verify(ArrayList<String> columnsOfPatients) {


        if (!columnsOfPatients.get(AIRColumnIndex).equals("BOT")) {
            for (ArrayList<String> patientRows : vaccinatedList) {
                if (isEqual(columnsOfPatients, patientRows)) {
                    GoogleManager.getSpreadsheets().get(0).editRow(GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(columnsOfPatients), AIRColumnIndex, "BOT");
                }
            }
        } else {
            //if equal then
            boolean hasBeenVaccinated = false;
            for (ArrayList<String> patientRows : vaccinatedList) {
                if (isEqual(columnsOfPatients, patientRows)) {
                    hasBeenVaccinated = true;
                    break;
                }
            }
            if (!hasBeenVaccinated && (!alreadyWarned(columnsOfPatients))) {
                Color color = new Color();
                color.setGreen(0f);
                color.setRed(1f);
                color.setBlue(0f);
                ArrayList<Request> requests = new ArrayList<>();
                requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(columnsOfPatients), AIRColumnIndex, color));
                try {
                    GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("#################################################################");
                warnedList.add(columnsOfPatients);


                System.out.println("This instance has not vaccinated the following Patient");
                System.out.println(columnsOfPatients.get(firstNameColumnIndex) + " " + columnsOfPatients.get(lastNamecolumnIndex));
                System.out.println("Please note: If the program was recently restarted bot has lost record of previous AIRs it has done");
                System.out.println("If you believe this to be the case, then there is no cause for concern.");

            }
        }
    }

    private static boolean alreadyWarned(ArrayList<String> columnsOfPatients) {
        for (ArrayList<String> patientRow : warnedList) {
            if (isEqual(patientRow, columnsOfPatients))
                return true;
        }

        return false;
    }

    public static void setSpreadsheet(Spreadsheet selectedSpreadsheet) {
        spreadsheet = selectedSpreadsheet;
    }

    static void connect() {
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("debuggerAddress", "127.0.0.1:9222");
        WebDriver driver = new ChromeDriver(options);
        WebDriverRunner.setWebDriver(driver);

        status = "Starting";

        connected = true;
        open("https://vaccines.digitalhealth.gov.au/healthcare-providers/s/patients-list-");

    }

    static void autoOn() {
        autoMode = true;
        status = "Running";

    }

    static void autoOff() {
        autoMode = false;
        status = "Stopped";

    }

    static void startLoop() {
        if (autoLoop == null) {
            autoLoop = new Thread(new AutomationLoop());
            autoLoop.start();
            status = "idle";
        }

        spreadsheet = GoogleManager.getSpreadsheets().get(0);
        Configuration.timeout = 8000;

        encounterColumnIndex = spreadsheet.getColumnIndex(controller.EncounterColumnName.getText());
        doseReadyColumnIndex = spreadsheet.getColumnIndex(controller.DoseReadyColumnName.getText());
        AIRColumnIndex = spreadsheet.getColumnIndex(controller.AIRColumnName.getText());
        vaccinatedColumnIndex = spreadsheet.getColumnIndex(controller.VaccinatedColumnName.getText());

        firstNameColumnIndex = spreadsheet.getColumnIndex(controller.FirstNameColumnName.getText());
        lastNamecolumnIndex = spreadsheet.getColumnIndex(controller.LastNameColumnName.getText());
        DOBColumnIndex = spreadsheet.getColumnIndex(controller.DOBColumnName.getText());
        numOfColums = spreadsheet.getCopyRows().get(0).size();
    }


    static int getReadyCount() {



        int i = 0;
        for (ArrayList<String> columnsOfPatient : spreadsheet.getCopyRows())
            if (isReady(columnsOfPatient))
                i++;


        int finalI = i;
        Platform.runLater(()-> {
            controller.readyCount.textProperty().set(String.valueOf(finalI));
            controller.failedCount.textProperty().set(String.valueOf(failedList.size()));
            controller.completedCount.textProperty().set(String.valueOf(vaccinatedList.size()));
            controller.botStatus.textProperty().set("Waiting...");

        });
        return i;
    }

    static ArrayList<String> getAReadyPatient() {
        //if no patient ready then return null
        for (ArrayList<String> columnsOfPatient : spreadsheet.getCopyRows()) {
            if (isReady(columnsOfPatient)) {
                return columnsOfPatient;
            }
        }
        return null;
    }

    private static boolean isEqual(ArrayList<String> person, ArrayList<String> person2) {
        return person.get(firstNameColumnIndex).equals(person2.get(firstNameColumnIndex))
                && person.get(lastNamecolumnIndex).equals(person2.get(lastNamecolumnIndex))
                && person.get(DOBColumnIndex).equals(person2.get(DOBColumnIndex));
    }

    private static boolean isReady(ArrayList<String> columnsOfPatient) {
        if (columnsOfPatient.size() != numOfColums)
            return false;

        //if failed then ignore
        for (int i = 0; i < failedList.size(); i++)
            if (isEqual(columnsOfPatient, failedList.get(i)))
                return false;

        //if vaccinated then ignore
        for (int i = 0; i < vaccinatedList.size(); i++)
            if (isEqual(columnsOfPatient, vaccinatedList.get(i)))
                return false;

        for (int i = 0; i < waitHistory.size(); i++)
            if (isEqual(columnsOfPatient, waitHistory.get(i)))
                if (waitingTime.get(0) < Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE))
                    return false;

        //if not eligible to get AIR then ignore
        if (columnsOfPatient.get(encounterColumnIndex).equals("") || columnsOfPatient.get(encounterColumnIndex).toLowerCase(Locale.ROOT).equals("x"))
            return false;

        if (columnsOfPatient.get(vaccinatedColumnIndex).equals("") || columnsOfPatient.get(vaccinatedColumnIndex).toLowerCase(Locale.ROOT).equals("x"))
            return false;

        if (columnsOfPatient.get(doseReadyColumnIndex).equals("") || columnsOfPatient.get(doseReadyColumnIndex).toLowerCase(Locale.ROOT).equals("x"))
            return false;

        return (columnsOfPatient.get(AIRColumnIndex).equals("") || columnsOfPatient.get(AIRColumnIndex) == null);

    }

    static void stepBot() {
        step = true;
    }

    static void doPatient() {
        try {



            //get next available row to vaccinate
            ArrayList<String> vacinatee = getAReadyPatient();

            Platform.runLater(() -> {
                controller.botStatus.textProperty().set("AIR-ing.");
                for (Object patient :  controller.table.getItems()){
                    Patient temp = (Patient) patient;
                    if (temp.getFirstName().equals(vacinatee.get(firstNameColumnIndex)))
                        if (temp.getLastName().equals(vacinatee.get(lastNamecolumnIndex)))
                            if (temp.getDOB().equals(vacinatee.get(DOBColumnIndex)))
                                controller.table.getSelectionModel().select(patient);

                }

            });

            ArrayList<Request> requests = new ArrayList<>();
            Color color = new Color();
            color.setRed(.68f);
            color.setGreen(.85f);
            color.setBlue(.90f);

            requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(vacinatee), AIRColumnIndex, color));
            GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());


            open("https://vaccines.digitalhealth.gov.au/healthcare-providers/s/patients-list-");
            String firstName = vacinatee.get(firstNameColumnIndex).toLowerCase(Locale.ROOT);
            String lastName = vacinatee.get(lastNamecolumnIndex).toLowerCase(Locale.ROOT);
            int dose = Integer.parseInt(vacinatee.get(doseReadyColumnIndex));
//vaccinate(firstName, lastName, dose)
            if (vaccinate(firstName, lastName, dose)) {
                color.setRed(0f);
                color.setGreen(1f);
                color.setBlue(0f);
                requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(vacinatee), AIRColumnIndex, color));
                GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());
                //dataHandler.setCell("AIR", "BOT", ColorProperty.getAsGreen().getGoogleColor(), vacinatee);
                spreadsheet.editRow(spreadsheet.getCopyRows().indexOf(vacinatee), AIRColumnIndex, "BOT");
                vaccinatedList.add(vacinatee);
                vaccinateOut.write(firstName + "@$" + lastName + "@$" + vacinatee.get(DOBColumnIndex));
                vaccinateOut.flush();
            } else {
                //make yellow and add to waiting list
                if (!isWainting(vacinatee)) {
                    color.setRed(1f);
                    color.setGreen(1f);
                    color.setBlue(0f);

                    requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(vacinatee), AIRColumnIndex, color));
                    GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());
                    waitHistory.add(vacinatee);
                    int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) * 60 + Calendar.getInstance().get(Calendar.MINUTE);
                    waitingTime.add(currentTime + waitTimeInMinutes);
                    //make Patient yellow
                    //dataHandler.setCell("AIR", ColorProperty.getAsYellow().getGoogleColor(), vacinatee);

                } else {
                    color.setRed(1f);
                    color.setGreen(165f/256f);
                    color.setBlue(0f);
                    requests.add(GoogleWrapper.getRequest(GoogleManager.getSpreadsheets().get(0).getSheetID(), GoogleManager.getSpreadsheets().get(0).getCopyRows().indexOf(vacinatee), AIRColumnIndex, color));
                    GoogleWrapper.applyRequests(requests, GoogleManager.getSpreadsheets().get(0).getSpreadsheetID());

                    failedList.add(vacinatee);
                    //dataHandler.setCell("AIR", ColorProperty.getAsOrange().getGoogleColor(), vacinatee);


                }
            }
        } catch (Exception a) {
            a.printStackTrace();
            System.out.println(a.getCause());


        }
    }

    private static boolean isWainting(ArrayList<String> vacinatee) {
        for (ArrayList<String> patient : spreadsheet.getCopyRows())
            if (isEqual(vacinatee, patient))
                return true;

        return false;
    }

    static boolean vaccinate(String firstName, String lastName, int dose) {
        //spreadsheet.setCell("AIR", new ColorProperty(.68f,.85f,.90f).getGoogleColor(), patient);
        boolean found = search(firstName, lastName);
        if (!found) {
            return false;
        }
        boolean vaccinated = apply(dose);
        return vaccinated;
    }

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
        String link = $(byXpath("//*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + firstName + "') and contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lastName + "')]")).getAttribute("href");
        open(link);

        return true;
    }

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
        $(byXpath(vaccineComboBox)).click();


        //SET VACCINE
        if (vaccineType.equals("Astrazen")) {
            //select astrazeneca
            $(byXpath(astrazenVaccine)).click();

        } else if (vaccineType.equals("Pfizer")) {
            //select pfizer
            $(byXpath(pfizerVaccine)).click();

        } else {
            return false;
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
        new JavaScript(executeJavaScript("window.scrollTo(0, document.body.scrollHeight)"));
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
        if (vaccineType.equals("Astrazen"))
            if ($(byXpath("//*[contains(text(),'Pfizer')]")).exists())
                return false;

        if (vaccineType.equals("Pfizer"))
            if ($(byXpath("//*[contains(text(),'Astrazen')]")).exists())
                return false;


        $(byXpath(vaccinateRecord)).click();

        return true;

    }

    private static boolean createRecord() {
        boolean createRecordButtonPresent = $(byXpath("/html/body/div[4]/div[2]/div/div[2]/div/div[1]/div/div/ul/li[3]/a")).exists();
        if (!createRecordButtonPresent) {
            return false;
        }
        $(byXpath("/html/body/div[4]/div[2]/div/div[2]/div/div[1]/div/div/ul/li[3]/a")).click();
        return true;
    }

    private static boolean isPatientFound(String firstName, String lastName) {
        try {
            $(byXpath("//*[contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + firstName + "') and contains(translate(text(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz'), '" + lastName + "')]")).should(exist);
        } catch (com.codeborne.selenide.ex.ElementNotFound e) {
            return false;
        }
        return true;
    }

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

    static void getWaitingAndFailed() {
        //load from json file
    }

    static class AutomationLoop implements Runnable {

        private final ArrayList<ArrayList<Object>> readyList = new ArrayList<>();

        @Override
        public void run() throws com.codeborne.selenide.ex.ElementNotFound {
            connect();
            while (spreadsheet.getCopyRows().size() == 0) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


            getInitialVaccinations();

            while (true) {


                verifyAll();
                if ((autoMode || step) && getReadyCount() > lagBehindPatients) {

                    doPatient();
                    step = false;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }


}
