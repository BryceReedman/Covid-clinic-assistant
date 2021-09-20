import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.ArrayList;

public interface canSetSettings {
    ArrayList<TextField> textFields = new ArrayList<>();
    ArrayList<ComboBox<String>> comboBoxes = new ArrayList<>();

    ArrayList<Label> comboBoxLabels = new ArrayList<>();
    ArrayList<Label> textFieldLabels = new ArrayList<>();

    void apply();

    void update();

    Object getName();

    default void setTextFields(ArrayList<String> textFieldSettings) {
        for (int i = 0; i < textFieldSettings.size(); i++){
            textFields.get(i).setText(textFieldSettings.get(i));
        }

    }


    default void setComboBoxSettings(ArrayList<String> comboBoxSettings) {
        for (int i = 0; i < comboBoxSettings.size(); i++){
            comboBoxes.get(i).getSelectionModel().select(comboBoxSettings.get(i));
        }
    }

    default Object getComboBoxSettings() {
        ArrayList<String> comboBoxSettings = new ArrayList<>();
        for (ComboBox<String> comboBox : comboBoxes){
            comboBoxSettings.add(comboBox.getSelectionModel().getSelectedItem());
        }
        return comboBoxSettings;
    }

    default Object getTextFieldSettings() {
        ArrayList<String> textFieldSettings = new ArrayList<>();
        for (TextField textField : textFields){
            textFieldSettings.add(textField.getText());
        }
        return textFieldSettings;
    }

}
