package vaccine.main;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    public Label vaccineSelection;
    public Label botStatus;
    public Label completedCount;
    public Label failedCount;
    public Label waitingCount;
    public ComboBox spreadsheetSelection;
    public ComboBox sheetSelection;
    public Button botConnectButton;
    public Button botStartButton;
    public Button botStopButton;
    public Button botStepButton;
    public TextField EncounterColumnName;
    public TextField DoseReadyColumnName;
    public TextField VaccinatedColumnName;
    public TextField AIRColumnName;
    public TextField rangeTextField;
    public Button applyButton;
    public TextField FirstNameColumnName;
    public TextField LastNameColumnName;
    public TextField DOBColumnName;
    public Label readyCount;
    public TableView table;
    public TableColumn firstNameCol;
    public TableColumn lastNameCol;
    public TableColumn DOBCol;
    public TableColumn EncCol;
    public TableColumn DoseCol;
    public TableColumn VaccCol;
    public TableColumn AIRCol;

    ObservableList<Patient> patients = FXCollections.observableArrayList();

    private Thread listThread;

    public void closeThreads() {
        if (listThread == null)
            return;


            listThread.interrupt();

    }

    class list implements Runnable{

        @Override
        public void run() {

            while(GoogleManager.getSpreadsheets().get(0).getRows().size() == 0){
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            int encounterColumnIndex = GoogleManager.getSpreadsheets().get(0).getColumnIndex(EncounterColumnName.getText());
            int doseReadyColumnIndex = GoogleManager.getSpreadsheets().get(0).getColumnIndex(DoseReadyColumnName.getText());
            int AIRColumnIndex = GoogleManager.getSpreadsheets().get(0).getColumnIndex(AIRColumnName.getText());
            int vaccinatedColumnIndex = GoogleManager.getSpreadsheets().get(0).getColumnIndex(VaccinatedColumnName.getText());

            int firstNameColumnIndex = GoogleManager.getSpreadsheets().get(0).getColumnIndex(FirstNameColumnName.getText());
            int lastNamecolumnIndex = GoogleManager.getSpreadsheets().get(0).getColumnIndex(LastNameColumnName.getText());
            int DOBColumnIndex = GoogleManager.getSpreadsheets().get(0).getColumnIndex(DOBColumnName.getText());




            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }

                while (true) {

                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    ArrayList<ArrayList<String>> rows = GoogleManager.getSpreadsheets().get(0).getCopyRows();
                    if (rows.size() == 0)
                        return;

                    while (table.getItems().size() < rows.size()) {
                        table.getItems().add(new Patient());
                    }
                    while (patients.size() > rows.size()) {
                        table.getItems().remove(table.getItems().size() - 1);
                    }
                    for (int i = 0; i < table.getItems().size(); i++) {


                        Patient patient = (Patient) table.getItems().get(i);
                        patient.setFirstName(rows.get(i).get(firstNameColumnIndex));
                        patient.setLastName(rows.get(i).get(lastNamecolumnIndex));
                        patient.setDOB(rows.get(i).get(DOBColumnIndex));
                        patient.setEnc(rows.get(i).get(encounterColumnIndex));
                        patient.setDose(rows.get(i).get(doseReadyColumnIndex));
                        patient.setVaccinate(rows.get(i).get(vaccinatedColumnIndex));
                        patient.setAIR(rows.get(i).get(AIRColumnIndex));
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        break;
                    }
                }







        }
    }
    public void rangeSet(ActionEvent actionEvent) {
    }

    public void sheetSet(ActionEvent actionEvent) {
    }

    public void spreadsheetSet(ActionEvent actionEvent) {
    }

    public void botConnect(ActionEvent actionEvent) {
        AIRAutomation.startLoop();
        botStartButton.setDisable(false);
        botConnectButton.setDisable(true);
        botStepButton.setDisable(false);
    }

    public void botStart(ActionEvent actionEvent) {
        AIRAutomation.start();
        botStartButton.setDisable(true);
        botStopButton.setDisable(false);

    }

    public void botStop(ActionEvent actionEvent) {
        AIRAutomation.stop();
        botStartButton.setDisable(false);
        botStopButton.setDisable(true);
    }

    public void botStep(ActionEvent actionEvent) {
        AIRAutomation.stepBot();
    }

    public void encounterSet(ActionEvent actionEvent) {
    }

    public void doseReadySet(ActionEvent actionEvent) {
    }

    public void vaccinatedSet(ActionEvent actionEvent) {
    }

    public void AIRSet(ActionEvent actionEvent) {
    }

    public void loadSavedSettings() throws FileNotFoundException {
        FileReader settings;
        try {
            settings = new FileReader("settings");
        } catch (FileNotFoundException e){
            return;
        }

        int character = 0;
        StringBuilder data = new StringBuilder();

        while (true) {
            try {
                if ((character = settings.read()) == -1) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            data.append((char) character);
        }
        String[] lines = data.toString().split(System.lineSeparator());

        rangeTextField.setText(lines[0].split("@")[1]);
        VaccinatedColumnName.setText(lines[1].split("@")[1]);
        DoseReadyColumnName.setText(lines[2].split("@")[1]);
        EncounterColumnName.setText(lines[3].split("@")[1]);
        AIRColumnName.setText(lines[4].split("@")[1]);
        DOBColumnName.setText(lines[5].split("@")[1]);
        FirstNameColumnName.setText(lines[6].split("@")[1]);
        LastNameColumnName.setText(lines[7].split("@")[1]);
    }

    public void saveSettings() throws IOException {
        FileWriter settings = new FileWriter("settings");
        settings.write("range:@" + rangeTextField.getText() + System.lineSeparator());
        settings.write("vaccinate:@"+ VaccinatedColumnName.getText() + System.lineSeparator());
        settings.write("dose_ready:@"+ DoseReadyColumnName.getText() + System.lineSeparator());
        settings.write("encounter:@"+ EncounterColumnName.getText() + System.lineSeparator());
        settings.write("air:@"+ AIRColumnName.getText() + System.lineSeparator());
        settings.write("dob:@"+ DOBColumnName.getText() + System.lineSeparator());
        settings.write("firstname:@"+ FirstNameColumnName.getText() + System.lineSeparator());
        settings.write("lastname:@"+ LastNameColumnName.getText() + System.lineSeparator());
        settings.close();

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            loadSavedSettings();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            GoogleWrapper.start();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        GoogleManager.start();

        Spreadsheet spreadsheet = new Spreadsheet();
        GoogleManager.addSpreadsheet(spreadsheet);

        spreadsheetSelection.getItems().addAll(GoogleWrapper.getNamesOfSpreadsheets());
        spreadsheetSelection.setOnAction((event -> {
            String spreadsheetName = (String) spreadsheetSelection.getSelectionModel().getSelectedItem();
            sheetSelection.getItems().clear();
            sheetSelection.getItems().addAll(GoogleWrapper.getSheetNamesOfSpreadsheet(spreadsheetName));
            enableCheck();
        }));
        sheetSelection.setOnAction((event -> {
            enableCheck();

        }));

        rangeTextField.setOnAction((event -> enableCheck()));


        firstNameCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("firstName"));


        lastNameCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("lastName"));


        DOBCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("DOB"));

        EncCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("enc"));

        DoseCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("dose"));

        VaccCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("vaccinate"));

        AIRCol.setCellValueFactory(new PropertyValueFactory<Patient, String>("AIR"));
        

        table.itemsProperty().set(patients);
        AIRAutomation.configure(this);
    }

    @FXML
    private void enableCheck() {
        if (sheetSelection.getSelectionModel().getSelectedItem() != null
                && spreadsheetSelection.getSelectionModel().getSelectedItem() != null
                && !(rangeTextField.getText().isEmpty() || rangeTextField.getText().equals(""))
                && EncounterColumnName.getText() != null
                && VaccinatedColumnName.getText() != null
                && DoseReadyColumnName.getText() != null
                && AIRColumnName.getText() != null
                && FirstNameColumnName.getText() != null
                && LastNameColumnName.getText() != null
                && DOBColumnName.getText() != null)
            enableApplyButtons();
        else
            disableApplyButtons();
    }

    private void disableApplyButtons() {
        applyButton.setDisable(true);
    }

    private void enableApplyButtons() {
        applyButton.setDisable(false);

    }

    public void apply(ActionEvent actionEvent) {
        try {
            saveSettings();
        } catch (IOException e) {
            e.printStackTrace();
        }
        GoogleManager.getSpreadsheets().get(0).setRange(sheetSelection.getSelectionModel().getSelectedItem() + "!" + rangeTextField.getText());
        GoogleManager.getSpreadsheets().get(0).setSpreadSheetID(GoogleWrapper.getID((String) spreadsheetSelection.getSelectionModel().getSelectedItem()));
        GoogleManager.getSpreadsheets().get(0).setsheetID(GoogleWrapper.getSheetID(sheetSelection.getSelectionModel().getSelectedItem().toString(), GoogleWrapper.getID((String) spreadsheetSelection.getSelectionModel().getSelectedItem())));

        enableBotControls();
        disableApplyButtons();
        disableEntryFields();


        if (((String) sheetSelection.getSelectionModel().getSelectedItem()).toLowerCase(Locale.ROOT).contains("az"))
            vaccineSelection.textProperty().set("Astrazen");

        else if (((String) sheetSelection.getSelectionModel().getSelectedItem()).toLowerCase().contains("pfizer"))
            vaccineSelection.textProperty().set("Pfizer");

        else throw new NullPointerException("VACCINE NOT SPECIFIED IN SPREADSHEET");

    listThread = new Thread(new list());
    listThread.start();
    }

    private void disableEntryFields() {
        spreadsheetSelection.setDisable(true);
        sheetSelection.setDisable(true);
        rangeTextField.setDisable(true);

        EncounterColumnName.setDisable(true);
        VaccinatedColumnName.setDisable(true);
        DoseReadyColumnName.setDisable(true);
        AIRColumnName.setDisable(true);
        FirstNameColumnName.setDisable(true);
        LastNameColumnName.setDisable(true);
        DOBColumnName.setDisable(true);
    }

    private void enableBotControls() {
        botConnectButton.setDisable(false);

    }
}
