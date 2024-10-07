import bio.ferlab.fhir.Fhavro;
import bio.ferlab.fhir.converter.ConverterUtils;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
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
import org.hl7.fhir.r4.model.DomainResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BaseFhavro {

    private static final FhirContext fhirContext;

    BaseFhavro() {
    }

    static {
        fhirContext = FhirContext.forR4();
    }

    protected <T extends DomainResource> void assertBaseResource(String name, SchemaMode schemaMode, DomainResource baseResource, Class<T> type) {
        Schema schema = Fhavro.loadSchema(name, schemaMode);
        assertBaseResource(schema, name, baseResource, type);
    }

    protected <T extends BaseResource> T loadExampleFromFile(String filename, Class<T> clazz) {
        URL resource = ClassLoader.getSystemClassLoader().getResource("examples/" + filename);
        if (resource == null) {
            throw new BadRequestException("The following example is not found: examples/" + filename);
        }

        try (InputStream inputStream = resource.openStream()) {
            return fhirContext.newJsonParser().parseResource(clazz, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    protected <T extends BaseResource> List<T> loadExamplesFromFile(String filename, Class<T> clazz) {
        URL resource = ClassLoader.getSystemClassLoader().getResource("examples/" + filename);
        if (resource == null) {
            throw new BadRequestException("The following example is not found: examples/" + filename);
        }

        try (InputStream inputStream = resource.openStream()) {
            ObjectMapper mapper = new ObjectMapper();
            Object[] participantJsonList = mapper.readValue(IOUtils.toString(inputStream, StandardCharsets.UTF_8), Object[].class);

            IParser jsonParser = fhirContext.newJsonParser();
            List<T> baseResources = new ArrayList<>();
            for (Object object : participantJsonList) {
                baseResources.add(jsonParser.parseResource(clazz, mapper.writeValueAsString(object)));
            }
            return baseResources;
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    protected static File serializeGenericRecord(Schema schema, String name, GenericRecord genericRecord) {
        try {
            createResultDirectory(name);
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

    private <T extends DomainResource> void assertBaseResource(Schema schema, String name, DomainResource baseResource, Class<T> type) {
        GenericRecord input = Fhavro.convertResourceToGenericRecord(baseResource, schema);
        File file = serializeGenericRecord(schema, name, input);

        GenericRecord output = null;
        try {
            output = deserializeGenericRecord(schema, file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        T result = Fhavro.convertGenericRecordToResource(output, schema, type.getSimpleName());

        String inputString = ConverterUtils.standardizeDate(fhirContext.newJsonParser().encodeResourceToString(baseResource));
        String outputString = ConverterUtils.standardizeDate(fhirContext.newJsonParser().encodeResourceToString(result));

        assertEquals(inputString, outputString);

        /* For Development purposes.
        try {
            ObjectMapper mapper = new ObjectMapper();
            Object jsonObject = mapper.readValue(outputString, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
            System.out.println(prettyJson);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        */
    }

    private static void createResultDirectory(String relativePath) {
        try {
            String before = relativePath.substring(0, relativePath.lastIndexOf("/") + 1);
            Files.createDirectories(Paths.get("./results/" + before));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
