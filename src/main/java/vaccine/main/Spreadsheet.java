package vaccine.main;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

public class Spreadsheet {
    final Object lock = new Object();
    boolean ready = false;
    ArrayList<ArrayList<String>> rows = new ArrayList<>();
    Dictionary<Integer, ArrayList<Integer>> editedRows = new Hashtable<>();
    private ArrayList<Integer> editedRowIndexes = new ArrayList<>();
    private String sheetName;
    private String spreadSheetID;
    private int sheetID;
    private String range;
    private boolean update = false;

    public Spreadsheet() {

    }


    public Spreadsheet(int sheetID, String spreadsheetID, String range) {
        this.spreadSheetID = spreadsheetID;
        this.sheetID = sheetID;
        this.range = range;
        this.sheetName = range.split("!")[0];


    }

    public synchronized ArrayList<ArrayList<String>> getRows() {
        synchronized (lock) {

            return rows;
        }
    }

    public void setRows(ArrayList<ArrayList<String>> rows) {
        synchronized (lock) {

            this.rows = rows;
        }
    }

    public synchronized void editRow(int rowIndex, int columnIndex, String content) {
        synchronized (lock) {
            if (!editedRowIndexes.contains(rowIndex))
                editedRowIndexes.add(rowIndex);

            update = true;
            //set rows
            rows.get(rowIndex).set(columnIndex, content);
            ArrayList<Integer> item = new ArrayList<>();
            item.add(columnIndex);

            if (editedRows.get(rowIndex) == null) {
                editedRows.put(rowIndex, item);
            } else {
                editedRows.get(rowIndex).add(columnIndex);
            }
        }
    }

    public Dictionary<Integer, ArrayList<Integer>> getEditedRows() {
        return editedRows;
    }

    public void clearEdits() {
        editedRows = new Hashtable<>();
        editedRowIndexes = new ArrayList<>();
        update = false;
    }

    public String getSpreadsheetID() {
        return spreadSheetID;
    }

    public void setSpreadSheetID(String ID) {
        this.spreadSheetID = ID;
        checkIsReady();
    }

    private void checkIsReady() {
        if (this.range != null && this.spreadSheetID != null)
            ready = true;
    }


    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
        checkIsReady();
    }

    public boolean needUpdate() {
        if (update) {
            update = false;
            return true;
        }
        return false;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String text) {
        this.sheetName = text;
        checkIsReady();

    }

    public Integer getColumnIndex(String text) {
        for (int i = 0; i < rows.get(0).size(); i++) {
            if (rows.get(0).get(i).equals(text)) {
                return i;
            }

        }
        return null;
    }

    public int getSheetID() {
        return sheetID;
    }

    public ArrayList<Integer> getEditedRowIndexes() {
        return editedRowIndexes;
    }

    public void setsheetID(int sheetID) {
        this.sheetID = sheetID;
    }

    public void setrow(int i, ArrayList<String> strings) {
        try {
            rows.get(i).clear();
            rows.get(i).addAll(strings);
        } catch (IndexOutOfBoundsException e) {
            if (i == strings.size())
                rows.add(strings);
            else
                throw new NullPointerException("2 over");
        }
    }

    public void update(ArrayList<ArrayList<String>> rows) {
        synchronized (lock) {
            while (rows.size() > this.rows.size())
                this.rows.add(new ArrayList<>());
            while (rows.size() < this.rows.size())
                this.rows.remove(-1);

            for (int i = 0; i < rows.size(); i++) {
                this.rows.set(i, rows.get(i));
            }
        }
    }

    public ArrayList<ArrayList<String>> getCopyRows() {
        synchronized (lock) {
            ArrayList<ArrayList<String>> copyOfRows = new ArrayList<>();
            for (ArrayList<String> patients : rows) {
                ArrayList<String> contentCopy = new ArrayList<>();
                for (String content : patients) {
                    String copy = content;
                    contentCopy.add(copy);
                }
                copyOfRows.add(contentCopy);
            }

            return copyOfRows;
        }
    }
}
