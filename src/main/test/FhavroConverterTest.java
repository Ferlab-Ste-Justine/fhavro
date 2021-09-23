import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.converter.AvroFhirConverter;
import bio.ferlab.fhir.converter.DateUtils;
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

    // Not working because value[x] are not serialized yet.
//    @Test
//    public void test_serialize_effectEvidenceSynthesis() {
//        assertBaseResource("EffectEvidenceSynthesis", EffectEvidenceSynthesisFixture.createEffectEvidenceSynthesis(), EffectEvidenceSynthesis.class);
//    }

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
        
        String inputString = convertDate(FhirContext.forR4().newJsonParser().encodeResourceToString(baseResource));
        String outputString = convertDate(FhirContext.forR4().newJsonParser().encodeResourceToString(result));

        // TODO improve the testing to see if two entities are the same because the encodeResourcetoString does not format Date properly.
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
