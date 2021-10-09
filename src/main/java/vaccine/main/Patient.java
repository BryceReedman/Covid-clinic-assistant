package vaccine.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Patient {
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty DOB = new SimpleStringProperty();
    private final StringProperty enc = new SimpleStringProperty();
    private final StringProperty dose = new SimpleStringProperty();
    private final StringProperty vaccinate = new SimpleStringProperty();
    private final StringProperty AIR = new SimpleStringProperty();

    public Patient() {
    }

    public String getFirstName() {
        return firstName.get();
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public String getLastName() {
        return lastName.get();
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public String getDOB() {
        return DOB.get();
    }

    public void setDOB(String DOB) {
        this.DOB.set(DOB);
    }

    public StringProperty DOBProperty() {
        return DOB;
    }

    public String getEnc() {
        return enc.get();
    }

    public void setEnc(String enc) {
        this.enc.set(enc);
    }

    public StringProperty encProperty() {
        return enc;
    }

    public String getDose() {
        return dose.get();
    }

    public void setDose(String dose) {
        this.dose.set(dose);
    }

    public StringProperty doseProperty() {
        return dose;
    }

    public String getVaccinate() {
        return vaccinate.get();
    }

    public void setVaccinate(String vaccinate) {
        this.vaccinate.set(vaccinate);
    }

    public StringProperty vaccinateProperty() {
        return vaccinate;
    }

    public String getAIR() {
        return AIR.get();
    }

    public void setAIR(String AIR) {
        this.AIR.set(AIR);
    }

    public StringProperty AIRProperty() {
        return AIR;
    }
}
