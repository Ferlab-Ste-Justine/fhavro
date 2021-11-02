package bio.ferlab.fhir;

import bio.ferlab.fhir.converter.AvroFhirConverter;
import bio.ferlab.fhir.converter.ConverterUtils;
import bio.ferlab.fhir.converter.FhirAvroConverter;
import bio.ferlab.fhir.converter.Operation;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.definition.BaseDefinition;
import bio.ferlab.fhir.schema.definition.SchemaDefinition;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import ca.uhn.fhir.context.FhirContext;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FhavroConverter {

    private static final FhirContext fhirContext;

    FhavroConverter() {
    }

    static {
        fhirContext = FhirContext.forR4();
    }

    @NotNull
    public static GenericData.Record convertResourceToGenericRecord(DomainResource baseResource, Schema schema) {
        return FhirAvroConverter.readResource(baseResource, schema);
    }

    @NotNull
    public static <T extends DomainResource> T convertGenericRecordToResource(GenericRecord genericRecord, Schema schema, String name) {
        return AvroFhirConverter.readGenericRecord(genericRecord, schema, name);
    }

    @NotNull
    public static Schema loadSchema(String schemaName, SchemaMode schemaMode) {
        schemaName = ConverterUtils.formatSchemaName(schemaName);

        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("schemas/" + schemaMode.toString().toLowerCase() + "/" + schemaName);
        if (inputStream == null) {
            throw new BadRequestException("The following schema is not found: " + schemaName);
        }

        try {
            return new Schema.Parser().parse(inputStream);
        } catch (IOException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    @NotNull
    public static StructureDefinition loadExtension(String filename) {
        return loadProfile("extensions/" + filename);
    }

    @NotNull
    public static StructureDefinition loadProfile(String filename) {
        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("profiles/" + filename);
        if (inputStream == null) {
            throw new BadRequestException("The following profile is not found: profiles/" + filename);
        }

        return loadProfile(inputStream);
    }

    @NotNull
    public static StructureDefinition loadProfile(InputStream inputStream) {
        try {
            return fhirContext.newJsonParser().parseResource(StructureDefinition.class, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    @NotNull
    public static String generateSchema(String schemaName, SchemaMode schemaMode) {
        return generateDefinition(schemaMode, new SchemaDefinition(schemaName));
    }

    @NotNull
    public static String generateSchema(String schemaName, SchemaMode schemaMode, StructureDefinition profile, List<StructureDefinition> extensions) {
        return generateDefinition(schemaMode, new SchemaDefinition(schemaName, profile, extensions));
    }

    public static void serializeGenericRecords(Schema schema, List<GenericRecord> genericRecords, OutputStream outputStream) {
        try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<>(schema))) {
            dataFileWriter.create(schema, outputStream);
            for (GenericRecord genericRecord : genericRecords) {
                dataFileWriter.append(genericRecord);
            }
        } catch (IOException ex) {
            throw new BadRequestException("Please verify the OutputStream.");
        }
    }

    public static List<GenericRecord> deserializeGenericRecords(Schema schema, File file) {
        DatumReader<GenericRecord> userDatumReader = new GenericDatumReader<>(schema);
        try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(file, userDatumReader)) {
            List<GenericRecord> list = new ArrayList<>();
            while (dataFileReader.hasNext()) {
                list.add(dataFileReader.next());
            }
            return list;
        } catch (IOException ex) {
            throw new BadRequestException("Please verify the Input File.");
        }
    }

    private static String generateDefinition(SchemaMode schemaMode, SchemaDefinition schemaDefinition) {
        DefinitionRepository.initialize(schemaMode);

        Operation<BaseDefinition> operation = DefinitionRepository.generateDefinition(schemaDefinition);
        if (operation.isValid()) {
            return operation.getResult().getJsonObject().toString();
        } else {
            throw new BadRequestException("The following schema is not found: " + schemaDefinition.getSchemaName());
        }
    }
}
