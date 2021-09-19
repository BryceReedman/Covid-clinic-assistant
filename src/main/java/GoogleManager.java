import com.google.api.services.sheets.v4.model.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;

public class GoogleManager {
    private static Thread updaterThread;
    private static Thread initializeThread;
    private static final ArrayList<Spreadsheet> spreadsheets = new ArrayList<>();

    public static void start(){
        initializeThread = new Thread(new initialize());
    }

    public static ArrayList<Spreadsheet> getSpreadsheets(){
        return spreadsheets;
    }

    public static void addSpreadsheet(Spreadsheet spreadsheet){
        spreadsheets.add(spreadsheet);
    }

    public static void removeList(Spreadsheet spreadsheet){
        spreadsheets.remove(spreadsheet);
    }

    private static void sendUpdates(Spreadsheet spreadsheet){
        if (!spreadsheet.needUpdate()){
            return;
        }

        ArrayList<Request> requests = new ArrayList<>();
        Dictionary<Integer, Dictionary<Integer, Object>> editedRows = spreadsheet.getEditedRows();

        ArrayList<Integer> editedRowIndexes = new ArrayList<>();

        while (editedRows.keys().hasMoreElements()){
            editedRowIndexes.add(editedRows.keys().nextElement());
        }

        for (int rowKey: editedRowIndexes){
            Dictionary<Integer, Object> editedColumns = editedRows.get(rowKey);
            ArrayList<Integer> editedColumnIndexes = new ArrayList<>();

            while (editedColumns.keys().hasMoreElements()){
                editedColumnIndexes.add(editedColumns.keys().nextElement());
            }

            for (int columnKey : editedColumnIndexes){
                requests.add(GoogleHandler.getRequest(spreadsheet.getID(),rowKey,columnKey, (String) editedColumns.get(columnKey)));

            }


        }

            try {
                GoogleHandler.applyRequests(requests, spreadsheet.getID());
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    private static void downloadList(Spreadsheet spreadsheet) throws IOException {
        spreadsheet.setRows(GoogleHandler.getSheet(spreadsheet.getID(), spreadsheet.getRange()));
    }

    private static class updater implements Runnable{

        @Override
        public void run() {
            for (Spreadsheet spreadsheet: spreadsheets){
                sendUpdates(spreadsheet);
            }

        }
    }

    private static class initialize implements Runnable{

        @Override
        public void run() {
            //initialize code

            for (Spreadsheet spreadsheet: spreadsheets){
                try {
                    downloadList(spreadsheet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            //updater
            updaterThread = new Thread(new updater());
        }
    }

}
