package bio.ferlab.fhir.schema.repository;

import bio.ferlab.fhir.converter.exception.BadRequestException;

import java.util.Arrays;

public enum SchemaMode {
    DEFAULT,            // A schema with all fields as describe in the Fhir protocol.
    SIMPLE,             // A schema without any Extensions to avoid recursive definition nor Cyclical definition.
    ADVANCED;           // A schema with specific Extensions has defined by its profile.

    public static SchemaMode parseSchemaMode(String schemaMode) {
        return Arrays.stream(SchemaMode.values())
                .filter(mode -> mode.name().equalsIgnoreCase(schemaMode))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("The schema mode does not exists: " + schemaMode));
    }
}
