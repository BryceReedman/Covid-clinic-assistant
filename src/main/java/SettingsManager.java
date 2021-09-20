import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SettingsManager {
    ArrayList<canSetSettings> settings = new ArrayList<>();

    private void addSetable(canSetSettings obj){
        settings.add(obj);
    }

    private void removeSetable(canSetSettings obj){
        settings.remove(obj);
    }

    private void apply(){
        for (canSetSettings settingsObject : settings){
            settingsObject.apply();
        }
    }

    private void update(){
        for (canSetSettings settingsObject : settings){
            settingsObject.update();
        }
    }

    public ArrayList<String> retrieveListOfConfigurables() {
        ArrayList<String> names = new ArrayList<>();
        for (canSetSettings settingsObject : settings) {
            names.add((String) settingsObject.getName());
        }
        return names;
    }

    public canSetSettings get(String name){
        for (canSetSettings settingsObject: settings){
            if (settingsObject.getName().equals(name))
                return settingsObject;
        }
        return null;
    }

    public void saveSettings() throws IOException {
        Type listOfStringsType = new TypeToken<List<String>>(){}.getType();

        update();

        for (canSetSettings settingsObject: settings){
            Gson gsonTextFieldSettings = new Gson();
            gsonTextFieldSettings.toJson(settingsObject.getTextFieldSettings(), listOfStringsType);

            Gson gsonComboBoxSettings = new Gson();
            gsonComboBoxSettings.toJson(settingsObject.getComboBoxSettings(), listOfStringsType);

            FileWriter fileTextFieldSettings = new FileWriter(""+ settingsObject.getName() +"TextFieldSettings.json");
            fileTextFieldSettings.write(gsonTextFieldSettings.toString());
            fileTextFieldSettings.close();

            FileWriter fileComboBoxSettings = new FileWriter(""+ settingsObject.getName() +"ComboBoxSettings.json");
            fileComboBoxSettings.write(gsonComboBoxSettings.toString());
            fileComboBoxSettings.close();
        }


    }

    public void loadSettings(){

        for (canSetSettings settingsObject: settings){
            Type listOfStringsType = new TypeToken<List<String>>(){}.getType();


            try {
                FileReader fileTextFieldSettings = new FileReader("" + settingsObject.getName() + "TextFieldSettings.json");
                Gson gsonTextFieldSettings = new Gson();
                ArrayList<String> TextFieldSettings = gsonTextFieldSettings.fromJson(fileTextFieldSettings.toString(), listOfStringsType);
                fileTextFieldSettings.close();

                FileReader fileComboBoxSettings = new FileReader("" + settingsObject.getName() + "ComboBoxSettings.json");
                Gson gsonComboBoxSettings = new Gson();
                ArrayList<String> ComboBoxSettings = gsonTextFieldSettings.fromJson(fileTextFieldSettings.toString(), listOfStringsType);
                fileComboBoxSettings.close();

                settingsObject.setTextFields(TextFieldSettings);
                settingsObject.setComboBoxSettings(ComboBoxSettings);
            } catch (IOException a) {
                System.out.println("WARNING: Settings File For " + settingsObject.getName() + " does not exist.");
            }


        }
        apply();


    }
}
