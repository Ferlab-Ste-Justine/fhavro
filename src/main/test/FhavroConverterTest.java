import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.converter.DateUtils;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fixture.*;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class FhavroConverterTest {

    @Test
    public void test_serialize_patient() {
        assertBaseResource("Patient", PatientFixture.createPatient(), Patient.class);
    }

    @Test
    public void test_serialize_patient_with_inner_extension() {
        Patient patient = PatientFixture.createPatient();
        List<Extension> extensions = ExtensionFixture.createExtensions();
        Extension extension = new Extension();
        extension.addExtension(extensions.get(0));
        extension.addExtension(extensions.get(1));
        patient.addExtension(extension);
        assertBaseResource("Patient", patient, Patient.class);
    }

    @Test
    public void test_serialize_cqdg_patient_examples() {
        List<String> examples = List.of("cqdg-Patient-example-1.json", "cqdg-Patient-example-2.json");
        for (String example : examples) {
            assertBaseResource("Patient", loadExampleFromFile(example, Patient.class), Patient.class);
        }
    }

    @Test
    public void test_serialize_ncpi_patient_examples() {
        List<String> examples = List.of("ncpi-Patient-example-1.json", "ncpi-Patient-example-2.json", "ncpi-Patient-example-3.json");
        for (String example : examples) {
            assertBaseResource("Patient", loadExampleFromFile(example, Patient.class), Patient.class);
        }
    }

    @Test
    public void test_serialize_ncpi_practitioner_example_1() {
        Practitioner practitioner = loadExampleFromFile("ncpi-Practitioner-example-1.json", Practitioner.class);
        assertBaseResource("Practitioner", practitioner, Practitioner.class);
    }

    @Test
    public void test_serialize_ncpi_practitioner_role_example_1() {
        PractitionerRole practitionerRole = loadExampleFromFile("ncpi-PractitionerRole-example-1.json", PractitionerRole.class);
        assertBaseResource("PractitionerRole", practitionerRole, PractitionerRole.class);
    }

    @Test
    public void test_serialize_ncpi_diagnostic_report_example_1() {
        DiagnosticReport diagnosticReport = loadExampleFromFile("ncpi-DiagnosticReport-example-1.json", DiagnosticReport.class);
        assertBaseResource("DiagnosticReport", diagnosticReport, DiagnosticReport.class);
    }

    // Not working, contains private fields (e.g: _receivedTime) with data.
    @Test
    public void test_serialize_ncpi_Specimen_example_1() {
        Specimen specimen = loadExampleFromFile("ncpi-Specimen-example-1.json", Specimen.class);
        assertBaseResource("Specimen", specimen, Specimen.class);
    }

    // Partially working, contains private field which are not serializable. (e.g: _recordedDate which is a <dateTime> recorded as a { dateTime },
    // profile does not describe this behaviour. If you do not consider those private fields, the test should pass.
    @Test
    public void test_serialize_ncpi_condition_disease_example_1() {
        Condition condition = loadExampleFromFile("ncpi-Disease-example-1.json", Condition.class);
        assertBaseResource("ncpi-disease", condition, Condition.class);
    }

    @Test
    public void test_serialize_ncpi_family_relationship() {
        List<String> examples = List.of("ncpi-FamilyRelationship-example-1.json", "ncpi-FamilyRelationship-example-2.json", "ncpi-FamilyRelationship-example-3.json", "ncpi-FamilyRelationship-example-4.json");
        for (String example : examples) {
            assertBaseResource("ncpi-family-relationship", loadExampleFromFile(example, Observation.class), Observation.class);
        }
    }

    @Test
    public void test_serialize_ncpi_document_reference() {
        DocumentReference documentReference = loadExampleFromFile("ncpi-DocumentReference-example-1.json", DocumentReference.class);
        assertBaseResource("DocumentReference", documentReference, DocumentReference.class);
    }

    @Test
    public void test_serialize_ncpi_organization() {
        Organization organization = loadExampleFromFile("ncpi-Organization-example-1.json", Organization.class);
        assertBaseResource("Organization", organization, Organization.class);
    }

    @Test
    public void test_serialize_ncpi_phenotype() {
        Condition condition = loadExampleFromFile("ncpi-Phenotype-example-1.json", Condition.class);
        assertBaseResource("Condition", condition, Condition.class);
    }

    @Test
    public void test_serialize_ncpi_research_study() {
        ResearchStudy researchStudy = loadExampleFromFile("ncpi-ResearchStudy-example-1.json", ResearchStudy.class);
        assertBaseResource("ResearchStudy", researchStudy, ResearchStudy.class);
    }

    @Test
    public void test_serialize_ncpi_research_subject() {
        List<String> examples = List.of("ncpi-ResearchSubject-example-1.json", "ncpi-ResearchSubject-example-2.json", "ncpi-ResearchSubject-example-3.json");
        for (String example : examples) {
            assertBaseResource("ResearchSubject", loadExampleFromFile(example, ResearchSubject.class), ResearchSubject.class);
        }
    }

    @Test
    public void test_serialize_condition() {
        assertBaseResource("Condition", PhenotypeFixture.createCondition(), Condition.class);
    }

    @Test
    public void test_serialize_appointment() {
        assertBaseResource("Appointment", AppointmentFixture.createAppointment(), Appointment.class);
    }

    @Test
    public void test_serialize_account() {
        assertBaseResource("Account", AccountFixture.createAccount(), Account.class);
    }

    @Test
    public void test_serialize_eventDefinition() {
        assertBaseResource("EventDefinition", EventDefinitionFixture.createEventDefinition(), EventDefinition.class);
    }

    @Test
    public void test_serialize_effectEvidenceSynthesis() {
        assertBaseResource("EffectEvidenceSynthesis", EffectEvidenceSynthesisFixture.createEffectEvidenceSynthesis(), EffectEvidenceSynthesis.class);
    }

    @Test
    public void test_serialize_evidenceVariable() {
        assertBaseResource("EvidenceVariable", EvidenceVariableFixture.createEvidenceVariable(), EvidenceVariable.class);
    }

    @Test
    public void test_serialize_carePlan() {
        assertBaseResource("CarePlan", CarePlanFixture.createCarePlan(), CarePlan.class);
    }

    private <T extends BaseResource> void assertBaseResource(String name, BaseResource baseResource, Class<T> type) {
        Schema schema = FhavroConverter.loadSchema(name);

        GenericRecord input = FhavroConverter.convertResourceToGenericRecord(baseResource, schema);
        File file = serializeGenericRecord(schema, name, input);

        GenericRecord output = null;
        try {
            output = deserializeGenericRecord(schema, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        T result = FhavroConverter.convertGenericRecordToResource(output, schema, type);

        FhirContext fhirContext = FhirContext.forR4();
        String inputString = convertDate(fhirContext.newJsonParser().encodeResourceToString(baseResource));
        String outputString = convertDate(fhirContext.newJsonParser().encodeResourceToString(result));

        assertEquals(inputString, outputString);

        try {
            ObjectMapper mapper = new ObjectMapper();
            Object jsonObject = mapper.readValue(outputString, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            System.out.println(prettyJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private <T extends BaseResource> T loadExampleFromFile(String filename, Class<T> clazz) {
        URL resource = ClassLoader.getSystemClassLoader().getResource("examples/" + filename);
        if (resource == null) {
            throw new BadRequestException("The following example is not found: examples/" + filename);
        }

        try (InputStream inputStream = resource.openStream()) {
            return FhirContext.forR4().newJsonParser().parseResource(clazz, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public static File serializeGenericRecord(Schema schema, String name, GenericRecord genericRecord) {
        try {
            File file = new File("./results/" + name + ".avro");
            DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<>(schema));
            dataFileWriter.create(schema, file);
            dataFileWriter.append(genericRecord);
            dataFileWriter.close();
            return file;
        } catch (IOException ex) {
            throw new RuntimeException("The following file couldn't be saved at ./results/: " + name);
        }
    }

    public static <K> K deserializeGenericRecord(Schema schema, File file) throws IOException {
        DatumReader<K> userDatumReader = new GenericDatumReader<>(schema);
        DataFileReader<K> dataFileReader = new DataFileReader<>(file, userDatumReader);
        K data = null;
        while (dataFileReader.hasNext()) {
            data = dataFileReader.next(data);
        }
        return data;
    }

    // The Json encoder does not format the date correctly (treat it as string) so this is a small hack just to avoid modifying the JsonParser().
    private static String convertDate(String date) {
        Matcher matcher = Pattern.compile("(?:[1-9]\\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d(?:Z|[+-][01]\\d:[0-5]\\d)").matcher(date);
        while (matcher.find()) {
            String group = matcher.group();
            String formattedGroup = DateUtils.formatTimestampMicros(DateUtils.toEpochSecond(group));
            date = date.replace(group, formattedGroup);
        }
        return date;
    }
}
