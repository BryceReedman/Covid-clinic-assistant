package vaccine.main;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.*;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class GoogleWrapper {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS + " " + DriveScopes.DRIVE_METADATA_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    //sheets
    private static final ArrayList<File> spreadsheets = new ArrayList<>();
    private static final Dictionary<String, ArrayList<Sheet>> sheetsOfSpreadsheet = new Hashtable<>();
    //service configurations
    private static Sheets sheetService;
    private static Drive driveService;

    //initialization code
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        String CREDENTIALS_FILE_PATH = "/credentials.json";
        InputStream in = GoogleWrapper.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        String TOKENS_DIRECTORY_PATH = "tokens";
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static ArrayList<File> downloadAllSpreadsheets() throws IOException {
        FileList result = driveService.files().list()
                .setPageSize(10)
                // Available Query parameters here:
                //https://developers.google.com/drive/v3/web/search-parameters
                .setQ("mimeType = 'application/vnd.google-apps.spreadsheet' and trashed = false")
                .setFields("nextPageToken, files(id, name)")
                .execute();

        return new ArrayList<>(result.getFiles());
    }

    private static ArrayList<Sheet> downloadAllSheets(String sheetID) throws IOException {
        Spreadsheet sp = sheetService.spreadsheets().get(sheetID).execute();
        return (ArrayList<Sheet>) sp.getSheets();
    }

    public static void applyRequests(ArrayList<Request> requests, String spreadsheetID) throws IOException {

        sheetService.spreadsheets().batchUpdate(spreadsheetID,
                        new BatchUpdateSpreadsheetRequest()
                                .setRequests(requests))
                .execute();
    }

    public static Request getRequest(int sheetID, int rowIndex, int columnIndex, Color cellColor) {
        ArrayList<CellData> cellDataList = new ArrayList<>();
        CellData cellData = new CellData();
        cellData.setUserEnteredFormat(new CellFormat().setBackgroundColor(cellColor));
        cellDataList.add(cellData);
        ArrayList<RowData> rowData = new ArrayList<>();
        rowData.add(new RowData().setValues(cellDataList));
        UpdateCellsRequest updateCellsRequest = new UpdateCellsRequest();
        updateCellsRequest.setRows(rowData);
        GridRange range = new GridRange();
        range.setSheetId(sheetID);
        range.setStartRowIndex(rowIndex);
        range.setEndRowIndex(rowIndex + 1);
        range.setStartColumnIndex(columnIndex);
        range.setEndColumnIndex(columnIndex + 1);
        updateCellsRequest.setRange(range);
        updateCellsRequest.setFields("UserEnteredFormat(BackgroundColor)");
        return new Request().setUpdateCells(updateCellsRequest);
    }

    public static Request getRequest(int sheetID, int rowIndex, int columnIndex, String content) {

        ArrayList<CellData> cellDataList = new ArrayList<>();
        ExtendedValue extendedValue = new ExtendedValue();
        extendedValue.setStringValue(content);

        CellData value = new CellData().setUserEnteredValue(extendedValue);
        cellDataList.add(value);

        ArrayList<RowData> rowDataList = new ArrayList<>();
        RowData rowData = new RowData().setValues(cellDataList);
        rowDataList.add(rowData);

        UpdateCellsRequest updateCellsRequest = new UpdateCellsRequest();
        updateCellsRequest.setRows(rowDataList);
        GridRange range = new GridRange();
        range.setSheetId(sheetID);
        range.setStartRowIndex(rowIndex);
        range.setEndRowIndex(rowIndex + 1);
        range.setStartColumnIndex(columnIndex);
        range.setEndColumnIndex(columnIndex + 1);
        updateCellsRequest.setRange(range);
        updateCellsRequest.setFields("userEnteredValue");
        return new Request().setUpdateCells(updateCellsRequest);
    }

    public static void start() throws GeneralSecurityException, IOException {
        //create google drive service
        //create google sheet service

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        String APPLICATION_NAME = "Clinic Assistant";
        sheetService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        //get all spreadsheets
        spreadsheets.addAll(downloadAllSpreadsheets());


        //get sheets of all spreadsheets
        for (File file : spreadsheets) {
            String sheetID = (file.getId());
            ArrayList<Sheet> sheets = downloadAllSheets(file.getId());
            sheetsOfSpreadsheet.put(sheetID, sheets);
        }
    }

    public static JsonArray getSheet(String spreadsheetId, String range) throws IOException {
        Sheets.Spreadsheets.Get request;

        List<String> ranges = new ArrayList<>();
        ranges.add(range);
        request = sheetService.spreadsheets().get(spreadsheetId);

        request.setRanges(ranges);
        request.setIncludeGridData(true);

        Object response = request.execute();
        JsonObject gridData = JsonParser.parseString(response.toString()).getAsJsonObject();

        return gridData.get("sheets").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("data").getAsJsonArray()
                .get(0).getAsJsonObject()
                .get("rowData").getAsJsonArray();
    }

    public static String getSpreadsheetName(int id) {
        for (File sheet : spreadsheets) {
            if (Integer.parseInt(sheet.getId()) == id)
                return sheet.getName();
        }
        return null;
    }

    public static String getID(String spreadsheetName) {
        for (File spreadsheet : spreadsheets) {
            if (spreadsheet.getName().equals(spreadsheetName))
                return spreadsheet.getId();
        }

        return null;
    }

    public static ArrayList<String> getNamesOfSpreadsheets() {
        ArrayList<String> spreadsheetNames = new ArrayList<>();
        for (File spreadsheet : spreadsheets) {
            spreadsheetNames.add(spreadsheet.getName());
        }
        return spreadsheetNames;
    }

    public static ArrayList<String> getSheetNamesOfSpreadsheet(String spreadsheetName) {
        ArrayList<String> listOfSheets = new ArrayList<>();
        for (Sheet sheet : sheetsOfSpreadsheet.get(getID(spreadsheetName))) {
            listOfSheets.add(sheet.getProperties().getTitle());
        }
        return listOfSheets;
    }

    public static int getSheetID(String s, String id) {
        for (Sheet sheet : sheetsOfSpreadsheet.get(id))
            if (sheet.getProperties().getTitle().equals(s))
                return sheet.getProperties().getSheetId();

        return 0;
    }
}
