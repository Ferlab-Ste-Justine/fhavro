package fixture;

import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PatientFixture {

    public static Patient createPatient() {
        Patient patient = new Patient();

        HumanName homerSimpson = new HumanName();
        homerSimpson.setFamily("Simpson");
        homerSimpson.addGiven("Homer");

        HumanName margeSimpson = new HumanName();
        homerSimpson.setFamily("Simpson");
        homerSimpson.addGiven("Marge");
        patient.addIdentifier(IdentifierFixture.createIdentifier());

        patient.addName(homerSimpson);
        patient.addName(margeSimpson);
        patient.setGender(Enumerations.AdministrativeGender.MALE);
        patient.setActive(true);
        patient.setMultipleBirth(new BooleanType(true));
        patient.setMaritalStatus(CodeableConceptFixture.createCodeableConcept());
        patient.setBirthDate(new Date());
        patient.setId(IdType.newRandomUuid());
        patient.addAddress(AddressFixture.createAddress());
        return patient;
    }
}
