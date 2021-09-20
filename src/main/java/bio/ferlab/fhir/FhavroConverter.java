package bio.ferlab.fhir;

import bio.ferlab.fhir.converter.AvroFhirConverter;
import bio.ferlab.fhir.converter.ConverterUtils;
import bio.ferlab.fhir.converter.FhirAvroConverter;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.hl7.fhir.r4.model.BaseResource;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.commons.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

public class FhavroConverter {

    private FhavroConverter() {}

    @NotNull
    public static GenericData.Record convertResourceToGenericRecord(BaseResource baseResource, Schema schema) {
        return FhirAvroConverter.readResource(baseResource, schema);
    }

    @NotNull
    public static <T extends BaseResource> T convertGenericRecordToResource(GenericRecord genericRecord, Schema schema, Class<T> type) {
        return AvroFhirConverter.readGenericRecord(genericRecord, schema, type);
    }

    @NotNull
    public static Schema loadSchema(String schemaName) {
        schemaName = ConverterUtils.formatSchemaName(schemaName);

        URL resource = ClassLoader.getSystemClassLoader().getResource("schemas/" + schemaName);
        if (resource == null) {
            throw new BadRequestException("The following schema is not found: " + schemaName);
        }

        try {
            return new Schema.Parser().parse(new File(resource.toURI()));
        } catch (IOException | URISyntaxException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }
}
