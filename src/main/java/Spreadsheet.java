import com.sun.glass.ui.Clipboard;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class Spreadsheet implements canSetSettings{
    final Object lock = new Object();

    private int ID;
    private String range;
    private boolean update = false;




    List<List<Object>> rows;
    Dictionary<Integer, Dictionary<Integer, Object>> editedRows = new Hashtable<>();

    public List<List<Object>> getRows(){
        synchronized (lock) {

            return rows;
        }
    }


    public void editRow(int rowIndex, int columnIndex, String content){
        synchronized (lock) {
            update = true;
            //set rows
            rows.get(rowIndex).set(columnIndex, content);

            //add edited rows
            Dictionary<Integer, Object> editedColumns;
            if (editedRows.get(rowIndex) == null)
                editedColumns = new Hashtable<>();
            else editedColumns = editedRows.get(rowIndex);

            editedColumns.put(columnIndex, content);
        }
    }

    public Dictionary<Integer, Dictionary<Integer, Object>> getEditedRows(){
        return editedRows;
    }

    public void setRows(List<List<Object>> rows){
        synchronized (lock) {

            this.rows = rows;
        }
    }

    public void clearEdits(){
        editedRows = new Hashtable<>();
    }

    public Spreadsheet(int ID, String range) {
        this.ID = ID;
        this.range = range;

        //create setting elements
        /*
         * ID
         * Range
         *
         *
         * */

        //sheetNameComboBox
        Label sheetNameLabel = new Label("SheetName");
        ComboBox<String> sheetNameComboBox = new ComboBox<>();
        sheetNameComboBox.setDisable(true);

        Label spreadsheetLabel = new Label("Spreadsheet");
        ComboBox<String> spreadsheetComboBox = new ComboBox<>();
        spreadsheetComboBox.getItems().addAll(GoogleHandler.getNamesOfSpreadsheets());
        //spreadsheetName
        spreadsheetComboBox.setOnAction((event -> {
            sheetNameComboBox.setDisable(false);
            sheetNameComboBox.getItems().addAll(GoogleHandler.getSheetNamesOfSpreadsheet(spreadsheetComboBox.getSelectionModel().getSelectedItem()));
        }));
        //range
        Label spreadsheetRangeLabel = new Label("Spreadsheet Range");
        TextField spreadsheetRangeTextFIeld = new TextField();

        comboBoxes.add(spreadsheetComboBox);
        comboBoxes.add(sheetNameComboBox);
        comboBoxLabels.add(spreadsheetLabel);
        comboBoxLabels.add(sheetNameLabel);

        textFields.add(spreadsheetRangeTextFIeld);
        textFieldLabels.add(spreadsheetRangeLabel);
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID){
        this.ID = ID;
    }


    public String getRange() {
        return range;
    }

    public void setRange(String range){
        this.range = range;
    }

    public boolean needUpdate() {
        if (update){
            update = false;
            return true;
        }
        return false;
    }

    @Override
    public void apply() {
        ID = GoogleHandler.getID(comboBoxes.get(0).getSelectionModel().getSelectedItem());
        range = comboBoxes.get(1).getSelectionModel().getSelectedItem() + "!" +textFields.get(0).getText();
    }

    @Override
    public void update() {
        comboBoxes.get(0).getSelectionModel().select(GoogleHandler.getSpreadsheetName(ID));

        String[] splitString = range.split("!");

        comboBoxes.get(1).getSelectionModel().select(splitString[0]);
        textFields.get(0).setText(splitString[1]);

    }

    @Override
    public Object getName() {
        return null;
    }




}
