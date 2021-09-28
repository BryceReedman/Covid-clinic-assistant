package vaccine.main;

import com.google.api.services.sheets.v4.model.Request;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;

public class GoogleManager {
    private static final ArrayList<Spreadsheet> spreadsheets = new ArrayList<>();
    public static boolean updateActive = false;




    private static Thread updaterThread;
    private static Thread initializeThread;

    public static void start() {
        initializeThread = new Thread(new initialize());
        initializeThread.start();
    }

    public static ArrayList<Spreadsheet> getSpreadsheets() {
        return spreadsheets;
    }


    public static void addSpreadsheet(Spreadsheet spreadsheet) {
        spreadsheets.add(spreadsheet);
    }

    public static void removeList(Spreadsheet spreadsheet) {
        spreadsheets.remove(spreadsheet);
    }

    private static void sendUpdates(Spreadsheet spreadsheet) {
        if (!spreadsheet.needUpdate()) {
            return;
        }

        ArrayList<Request> requests = new ArrayList<>();
        Dictionary<Integer, ArrayList<Integer>> editedData = spreadsheet.getEditedRows();

        ArrayList<Integer> editedRows = spreadsheet.getEditedRowIndexes();

        for (int rowKey : editedRows) {
            for (Integer editedColumn : editedData.get(rowKey))
                requests.add(GoogleWrapper.getRequest(spreadsheet.getSheetID(), rowKey, editedColumn, spreadsheet.getRows().get(rowKey).get(editedColumn)));
        }

        try {
            GoogleWrapper.applyRequests(requests, spreadsheet.getSpreadsheetID());
        } catch (IOException e) {
            e.printStackTrace();
        }
        spreadsheet.clearEdits();

    }

    private static void downloadList(Spreadsheet spreadsheet) throws IOException {
        JsonArray rawData = GoogleWrapper.getSheet(spreadsheet.getSpreadsheetID(), spreadsheet.getRange());
        ArrayList<ArrayList<String>> rows = new ArrayList<>();

        ArrayList<String> columnNames = new ArrayList<>();

        for (JsonElement row : rawData) {
            JsonArray columns = row.getAsJsonObject().get("values").getAsJsonArray();

            ArrayList<String> content = new ArrayList<>();
            for (JsonElement value : columns) {
                content.add(getStringValue(value.getAsJsonObject()));
            }
            rows.add(content);
        }

        ArrayList<ArrayList<String>> savedList = new ArrayList<>();
        ArrayList<Integer> indexes = new ArrayList<>();
        int editedIndex = 0;

        for (Integer i : spreadsheet.getEditedRowIndexes()) {
            savedList.add(spreadsheet.getRows().get(i));
            indexes.add(i);
        }


        spreadsheet.update(rows);


        for (Integer i : indexes) {
            spreadsheet.getRows().set(i, savedList.get(editedIndex));
            editedIndex++;
        }
    }


    /**
     * Returns the string value of the cell by parsing the numberValue of a cell.
     * If in event that the numberValue does not exist, then calls getStringValue()
     *
     * @param object the JsonObject to be parsed
     * @return the number value of the cell as a string
     */
    public static String getNumberValue(JsonObject object) {
        try {
            return object.get("effectiveValue").getAsJsonObject().get("numberValue").getAsString();
        } catch (NullPointerException a) {
            return "";
        }
    }

    public static String getFormattedValue(JsonObject object) {
        try {
            return object.get("formattedValue").getAsString();
        } catch (NullPointerException a) {
            return getStringValue(object);
        }
    }

    /**
     * Returns the string value of the JsonObject by parsing it as stringValue
     *
     * @param object the JsonObject to be parsed
     * @return the string value of the cell as a string
     */
    public static String getStringValue(JsonObject object) {
        try {
            return object.get("effectiveValue").getAsJsonObject().get("stringValue").getAsString();
        } catch (NullPointerException a) {
            return getNumberValue(object);
        }
    }

    public static void closeThreads() {
        if (updaterThread == null)
            return;

        updaterThread.interrupt();
        if (initializeThread == null)
            return;
        initializeThread.interrupt();
    }


    private static class updater implements Runnable {

        @Override
        public void run() {

            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    break;
                }


                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                if (!spreadsheets.isEmpty()) {
                    for (Spreadsheet spreadsheet : spreadsheets) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            break;
                        }

                        try {
                            if (spreadsheet.ready) {
                                sendUpdates(spreadsheet);
                                downloadList(spreadsheet);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    }

    private static class initialize implements Runnable {

        @Override
        public void run() {
            //initialize code

            while (!allSpreadsheetsReady()) {
                try {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            for (Spreadsheet spreadsheet : spreadsheets) {
                try {
                    if (spreadsheet.ready) {
                        downloadList(spreadsheet);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


            //updater
            updaterThread = new Thread(new updater());
            updaterThread.start();
        }

        private boolean allSpreadsheetsReady() {
            if (spreadsheets.isEmpty())
                return false;

            for (Spreadsheet spreadsheet : spreadsheets) {
                if (!spreadsheet.ready)
                    return false;
            }

            return true;
        }


    }
}




