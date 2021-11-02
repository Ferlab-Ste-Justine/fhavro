import bio.ferlab.fhir.schema.repository.SchemaMode;
import fixture.*;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class FhavroConverterDefaultTest extends BaseFhavroConverter {

    @Test
    public void test_serialize_patient() {
        assertBaseResource("Patient", SchemaMode.DEFAULT, PatientFixture.createPatient(), Patient.class);
    }

    @Test
    public void test_serialize_patient_with_multiple_extension() {
        Patient patient = PatientFixture.createPatient();
        patient.addExtension("test", new BooleanType(false));
        assertBaseResource("Patient", SchemaMode.DEFAULT, patient, Patient.class);
    }

    @Test
    public void test_serialize_patient_with_inner_extension() {
        Patient patient = PatientFixture.createPatient();
        List<Extension> extensions = ExtensionFixture.createExtensions();
        Extension extension = new Extension();
        extension.addExtension(extensions.get(0));
        extension.addExtension(extensions.get(1));
        patient.addExtension(extension);
        patient.addExtension(ExtensionFixture.createExtension(new StringType("Unknown")));
        assertBaseResource("Patient", SchemaMode.DEFAULT, patient, Patient.class);
    }

    @Test
    public void test_serialize_patient_with_extensions_arrays() {
        assertBaseResource("Patient", SchemaMode.DEFAULT, PatientFixture.createPatientWithExtensionsArray(), Patient.class);
    }

    @Test
    public void test_serialize_patient_with_relative_identifier() {
        assertBaseResource("Patient", SchemaMode.DEFAULT, PatientFixture.createPatientWithRelativeReference(), Patient.class);
    }

    @Test
    public void test_serialize_fhir_condition_examples() {
        List<String> examples = List.of("fhir-Condition-example-1.json");
        for (String example : examples) {
            assertBaseResource("Condition", SchemaMode.DEFAULT, loadExampleFromFile(example, Condition.class), Condition.class);
        }
    }

    @Test
    public void test_serialize_ncpi_patient_examples() {
        List<String> examples = List.of("ncpi-Patient-example-1.json", "ncpi-Patient-example-2.json", "ncpi-Patient-example-3.json");
        for (String example : examples) {
            assertBaseResource("Patient", SchemaMode.DEFAULT, loadExampleFromFile(example, Patient.class), Patient.class);
        }
    }

    @Test
    public void test_serialize_ncpi_practitioner_example_1() {
        Practitioner practitioner = loadExampleFromFile("ncpi-Practitioner-example-1.json", Practitioner.class);
        assertBaseResource("Practitioner", SchemaMode.DEFAULT, practitioner, Practitioner.class);
    }

    @Test
    public void test_serialize_ncpi_practitioner_role_example_1() {
        PractitionerRole practitionerRole = loadExampleFromFile("ncpi-PractitionerRole-example-1.json", PractitionerRole.class);
        assertBaseResource("PractitionerRole", SchemaMode.DEFAULT, practitionerRole, PractitionerRole.class);
    }

    @Test
    public void test_serialize_ncpi_diagnostic_report_example_1() {
        DiagnosticReport diagnosticReport = loadExampleFromFile("ncpi-DiagnosticReport-example-1.json", DiagnosticReport.class);
        assertBaseResource("DiagnosticReport", SchemaMode.DEFAULT, diagnosticReport, DiagnosticReport.class);
    }

    @Test
    public void test_serialize_ncpi_document_reference() {
        DocumentReference documentReference = loadExampleFromFile("ncpi-DocumentReference-example-1.json", DocumentReference.class);
        assertBaseResource("DocumentReference", SchemaMode.DEFAULT, documentReference, DocumentReference.class);
    }

    @Test
    public void test_serialize_ncpi_organization() {
        Organization organization = loadExampleFromFile("ncpi-Organization-example-1.json", Organization.class);
        assertBaseResource("Organization", SchemaMode.DEFAULT, organization, Organization.class);
    }

    @Test
    public void test_serialize_ncpi_research_study() {
        ResearchStudy researchStudy = loadExampleFromFile("ncpi-ResearchStudy-example-1.json", ResearchStudy.class);
        assertBaseResource("ResearchStudy", SchemaMode.DEFAULT, researchStudy, ResearchStudy.class);
    }


    @Test
    public void test_serialize_ncpi_research_subject() {
        List<String> examples = List.of("ncpi-ResearchSubject-example-1.json", "ncpi-ResearchSubject-example-2.json", "ncpi-ResearchSubject-example-3.json");
        for (String example : examples) {
            assertBaseResource("ResearchSubject", SchemaMode.DEFAULT, loadExampleFromFile(example, ResearchSubject.class), ResearchSubject.class);
        }
    }

    @Test
    public void test_serialize_condition() {
        assertBaseResource("Condition", SchemaMode.DEFAULT, PhenotypeFixture.createCondition(), Condition.class);
    }

    @Test
    public void test_serialize_appointment() {
        assertBaseResource("Appointment", SchemaMode.DEFAULT, AppointmentFixture.createAppointment(), Appointment.class);
    }

    @Test
    public void test_serialize_account() {
        assertBaseResource("Account", SchemaMode.DEFAULT, AccountFixture.createAccount(), Account.class);
    }

    @Test
    public void test_serialize_eventDefinition() {
        assertBaseResource("EventDefinition", SchemaMode.DEFAULT, EventDefinitionFixture.createEventDefinition(), EventDefinition.class);
    }

    @Test
    public void test_serialize_effectEvidenceSynthesis() {
        assertBaseResource("EffectEvidenceSynthesis", SchemaMode.DEFAULT, EffectEvidenceSynthesisFixture.createEffectEvidenceSynthesis(), EffectEvidenceSynthesis.class);
    }

    @Test
    public void test_serialize_evidenceVariable() {
        assertBaseResource("EvidenceVariable", SchemaMode.DEFAULT, EvidenceVariableFixture.createEvidenceVariable(), EvidenceVariable.class);
    }

    @Test
    public void test_serialize_carePlan() {
        assertBaseResource("CarePlan", SchemaMode.DEFAULT, CarePlanFixture.createCarePlan(), CarePlan.class);
    }

//    // Not working, contains private fields (e.g: _receivedTime) with data.
//    @Test
//    public void test_serialize_ncpi_Specimen_example_1() {
//        Specimen specimen = loadExampleFromFile("ncpi-Specimen-example-1.json", Specimen.class);
//        assertBaseResource("Specimen", SchemaMode.DEFAULT, specimen, Specimen.class);
//    }

    // TODO FIX THIS.
    // Does not work; deceasedDateTime is converted into deceasedBoolean ?
//    @Test
//    public void test_serialize_fhir_patient_examples() {
//        List<String> examples = List.of("fhir-Patient-example-1.json");
//        for (String example : examples) {
//            assertBaseResource("Patient", SchemaMode.DEFAULT, loadExampleFromFile(example, Patient.class), Patient.class);
//        }
//    }
}
