import bio.ferlab.fhir.schema.utils.SchemaUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class SchemaUtilsTest {

    @Test
    public void test_parseBaseDefinition() {
        assertEquals("Patient", SchemaUtils.parseBaseDefinition("http://hl7.org/fhir/StructureDefinition/Patient"));
    }

    @Test
    public void test_parsePropertyName() {
        assertEquals("maritalStatus", SchemaUtils.parsePropertyName("Patient.maritalStatus"));
    }

    @Test
    public void test_parsePropertyName_with_x() {
        assertEquals("multipleBirth[x]", SchemaUtils.parsePropertyName("Patient.multipleBirth[x]"));
    }
}
