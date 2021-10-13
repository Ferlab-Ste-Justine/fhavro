package bio.ferlab.fhir;

import bio.ferlab.fhir.converter.AvroFhirConverter;
import bio.ferlab.fhir.converter.ConverterUtils;
import bio.ferlab.fhir.converter.FhirAvroConverter;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import ca.uhn.fhir.context.FhirContext;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FhavroConverter {

    private static final FhirContext fhirContext;

    FhavroConverter() {
    }

    static {
        fhirContext = FhirContext.forR4();
    }

    @NotNull
    public static GenericData.Record convertResourceToGenericRecord(BaseResource baseResource, Schema schema) {
        return FhirAvroConverter.readResource(baseResource, schema);
    }

    @NotNull
    public static <T extends BaseResource> T convertGenericRecordToResource(GenericRecord genericRecord, Schema schema, String name) {
        return AvroFhirConverter.readGenericRecord(genericRecord, schema, name);
    }

    @NotNull
    public static Schema loadSchema(String schemaName) {
        schemaName = ConverterUtils.formatSchemaName(schemaName);

        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("schemas/" + schemaName);
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

        try {
            return fhirContext.newJsonParser().parseResource(StructureDefinition.class, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }
}
