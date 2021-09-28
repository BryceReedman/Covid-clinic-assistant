package vaccine.main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Patient {
    public String getFirstName() {
        return firstName.get();
    }

    public StringProperty firstNameProperty() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }

    public String getLastName() {
        return lastName.get();
    }

    public StringProperty lastNameProperty() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }

    public String getDOB() {
        return DOB.get();
    }

    public StringProperty DOBProperty() {
        return DOB;
    }

    public void setDOB(String DOB) {
        this.DOB.set(DOB);
    }

    public String getEnc() {
        return enc.get();
    }

    public StringProperty encProperty() {
        return enc;
    }

    public void setEnc(String enc) {
        this.enc.set(enc);
    }

    public String getDose() {
        return dose.get();
    }

    public StringProperty doseProperty() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose.set(dose);
    }

    public String getVaccinate() {
        return vaccinate.get();
    }

    public StringProperty vaccinateProperty() {
        return vaccinate;
    }

    public void setVaccinate(String vaccinate) {
        this.vaccinate.set(vaccinate);
    }

    public String getAIR() {
        return AIR.get();
    }

    public StringProperty AIRProperty() {
        return AIR;
    }

    public void setAIR(String AIR) {
        this.AIR.set(AIR);
    }

    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final StringProperty DOB = new SimpleStringProperty();
    private final StringProperty enc =  new SimpleStringProperty();
    private final StringProperty dose = new SimpleStringProperty();
    private final StringProperty vaccinate = new SimpleStringProperty();
    private final StringProperty AIR = new SimpleStringProperty();

    public Patient() {
    }
}
