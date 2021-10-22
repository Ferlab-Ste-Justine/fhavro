package fixture;

import bio.ferlab.fhir.schema.repository.SchemaMode;
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

    public static Patient createSimplePatient() {
        Patient patient = createPatient();
        patient.setExtension(null);
        return patient;
    }

    public static Patient createPatientWithExtensionsArray() {
        Patient patient = createSimplePatient();

        Extension outerExtension1 = new Extension();
        outerExtension1.setUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-race");
        outerExtension1.addExtension(new Extension( "text", new StringType("Unknown")));
        outerExtension1.addExtension(new Extension( "ombCategory", new Coding("http://terminology.hl7.org/CodeSystem/v3-NullFlavor", "UNK", "Unknown")));

        patient.addExtension(outerExtension1);

        Extension outerExtension2 = new Extension();
        outerExtension2.setUrl("http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity");
        outerExtension2.addExtension(new Extension("text", new StringType("Not Hispanic or Latino")));
        outerExtension2.addExtension(new Extension("ombCategory", new Coding("urn:oid:2.16.840.1.113883.6.238", "2186-5", "Not Hispanic or Latino")));

        Extension outerExtension3 = new Extension();
        outerExtension3.setUrl("test");
        outerExtension3.addExtension(outerExtension2);

        patient.addExtension(outerExtension3);


        return patient;
    }
}
