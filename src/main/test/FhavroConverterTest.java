import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.converter.AvroFhirConverter;
import bio.ferlab.fhir.converter.FhirAvroConverter;
import ca.uhn.fhir.context.FhirContext;
import fixture.*;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class FhavroConverterTest {

    @Test
    public void test_serialize_patient() {
        assertBaseResource("Patient", PatientFixture.createPatient(), Patient.class);
    }

    @Test
    public void test_serialize_appointment() {
        assertBaseResource("Appointment", AppointmentFixture.createAppointment(), Appointment.class);
    }

    @Test
    public void test_serialize_account() {
        assertBaseResource("Account", AccountFixture.createAccount(), Account.class);
    }

    // Not working because one of the symbols ("<") does not respect the Avro naming convention
    @Test
    public void test_serialize_effectEvidenceSynthesis() {
        assertBaseResource("EffectEvidenceSynthesis", EffectEvidenceSynthesisFixture.createEffectEvidenceSynthesis(), EffectEvidenceSynthesis.class);
    }

    // Not working because one of the symbols ("text/cql") does not respect the Avro naming convention
    @Test
    public void test_serialize_eventDefinition() {
        assertBaseResource("EventDefinition", EventDefinitionFixture.createEventDefinition(), EventDefinition.class);
    }

    // Not working because one of the symbols ("<" located in comparator of Quantity) does not respect the Avro naming convention
    @Test
    public void test_serialize_evidenceVariable() {
        assertBaseResource("EvidenceVariable", EvidenceVariableFixture.createEvidenceVariable(), EvidenceVariable.class);
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

        String inputString = FhirContext.forR4().newJsonParser().encodeResourceToString(baseResource);
        String outputString = FhirContext.forR4().newJsonParser().encodeResourceToString(result);

        assertEquals(inputString, outputString);
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
}
