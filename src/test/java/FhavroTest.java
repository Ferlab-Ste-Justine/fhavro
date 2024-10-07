import bio.ferlab.fhir.Fhavro;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FhavroTest {

    @Test
    public void loadSchema_simple() {
        assertNotNull(Fhavro.loadSchema("Patient", SchemaMode.SIMPLE));
    }

    @Test
    public void loadSchema_simple_with_relative_path() {
        assertNotNull(Fhavro.loadSchema("./Patient", SchemaMode.SIMPLE));
    }

    @Test
    public void loadSchema_default() {
        assertNotNull(Fhavro.loadSchema("Patient", SchemaMode.DEFAULT));
    }

    @Test
    public void loadSchema_default_with_relative_path() {
        assertNotNull(Fhavro.loadSchema("./Patient", SchemaMode.SIMPLE));
    }

    @Test
    public void loadSchema_advanced_from_known_location() {
        assertNotNull(Fhavro.loadSchema("schema/cqgc-patient", SchemaMode.ADVANCED));
    }

    @Test
    public void loadSchema_advanced_from_known_location_with_relative_path() {
        assertNotNull(Fhavro.loadSchema("./schema/cqgc-patient", SchemaMode.ADVANCED));
    }

    @Test
    public void loadSchema_advanced_from_known_location_with_relative_path_and_file_extension() {
        assertNotNull(Fhavro.loadSchema("./schema/cqgc-patient.avsc", SchemaMode.ADVANCED));
    }

    @Test
    public void loadSchema_advanced_from_unknown_location() {
        assertThrows(BadRequestException.class, () -> Fhavro.loadSchema("cqdg-patient", SchemaMode.ADVANCED));
    }
}
