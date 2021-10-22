package fixture;

import org.hl7.fhir.r4.model.*;

import java.util.ArrayList;
import java.util.Date;

public class PatientFixture {

    public static Patient createPatient() {
        Patient patient = new Patient();

        HumanName homerSimpson = new HumanName();
        homerSimpson.setFamily("Simpson");
        homerSimpson.addGiven("Homer");

        HumanName margeSimpson = new HumanName();
        homerSimpson.setFamily("Simpson");
        homerSimpson.addGiven("Marge");
        patient.setText(NarrativeFixture.createNarrative());
        patient.addIdentifier(IdentifierFixture.createIdentifier());
        patient.addIdentifier(IdentifierFixture.createDifferentIdentifier());
        patient.addExtension(ExtensionFixture.createExtension(new PositiveIntType(45)));
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

    public static Patient createPatientWithRelativeReference() {
        Patient patient = createPatient();
        patient.setIdentifier(new ArrayList<>());
        patient.addIdentifier(IdentifierFixture.createRelativeIdentifier());
        return patient;
    }
}
