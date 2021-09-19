import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class Spreadsheet {
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

    public Spreadsheet(int ID, String range){
        this.ID = ID;
        this.range = range;
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
}
