import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.converter.ConverterUtils;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import ca.uhn.fhir.context.FhirContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.BaseResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class BaseFhavroConverter {

    protected <T extends BaseResource> void assertBaseResource(String name, SchemaMode schemaMode, BaseResource baseResource, Class<T> type) {
        Schema schema = FhavroConverter.loadSchema(name, schemaMode);

        GenericRecord input = FhavroConverter.convertResourceToGenericRecord(baseResource, schema);
        File file = serializeGenericRecord(schema, name, input);

        GenericRecord output = null;
        try {
            output = deserializeGenericRecord(schema, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        T result = FhavroConverter.convertGenericRecordToResource(output, schema, type.getSimpleName());

        FhirContext fhirContext = FhirContext.forR4();
        String inputString = ConverterUtils.standardizeDate(fhirContext.newJsonParser().encodeResourceToString(baseResource));
        String outputString = ConverterUtils.standardizeDate(fhirContext.newJsonParser().encodeResourceToString(result));

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

    protected <T extends BaseResource> T loadExampleFromFile(String filename, Class<T> clazz) {
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

    protected static File serializeGenericRecord(Schema schema, String name, GenericRecord genericRecord) {
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

    protected static <K> K deserializeGenericRecord(Schema schema, File file) throws IOException {
        DatumReader<K> userDatumReader = new GenericDatumReader<>(schema);
        DataFileReader<K> dataFileReader = new DataFileReader<>(file, userDatumReader);
        K data = null;
        while (dataFileReader.hasNext()) {
            data = dataFileReader.next(data);
        }
        return data;
    }
}
