import java.util.ArrayList;

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

    public void saveSettings(){

    }

    public void loadSettings(){

    }
}
