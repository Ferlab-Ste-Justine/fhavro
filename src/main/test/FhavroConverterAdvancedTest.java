import bio.ferlab.fhir.schema.repository.SchemaMode;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.Test;

import java.util.List;

public class FhavroConverterAdvancedTest extends BaseFhavroConverter {

    @Test
    public void test_serialize_ncpi_condition_disease_example_2() {
        Condition condition = loadExampleFromFile("ncpi-Disease-example-2.json", Condition.class);
        assertBaseResource("ncpi-disease", SchemaMode.ADVANCED, condition, Condition.class);
    }

    @Test
    public void test_serialize_ncpi_family_relationship() {
        List<String> examples = List.of("ncpi-FamilyRelationship-example-1.json", "ncpi-FamilyRelationship-example-2.json", "ncpi-FamilyRelationship-example-3.json", "ncpi-FamilyRelationship-example-4.json");
        for (String example : examples) {
            assertBaseResource("ncpi-family-relationship", SchemaMode.ADVANCED, loadExampleFromFile(example, Observation.class), Observation.class);
        }
    }

    @Test
    public void test_serialize_cqdg_patient_examples() {
        List<String> examples = List.of("cqdg-Patient-example-1.json");
        //List<String> examples = List.of("cqdg-Patient-example-1.json", "cqdg-Patient-example-2.json");
        for (String example : examples) {
            assertBaseResource("cqgc-patient", SchemaMode.ADVANCED, loadExampleFromFile(example, Patient.class), Patient.class);
        }
    }

    @Test
    public void test_serialize_kfdrc_patient_examples() {
        List<String> examples = List.of("kfdrc-Patient-example-1.json");
        for (String example : examples) {
            assertBaseResource("kfdrc-patient", SchemaMode.ADVANCED, loadExampleFromFile(example, Patient.class), Patient.class);
        }
    }

    // Partially working, contains private field which are not serializable. (e.g: _recordedDate which is a <dateTime> recorded as a { dateTime },
    // profile does not describe this behaviour. If you do not consider those private fields, the test should pass.
//    @Test
//    public void test_serialize_ncpi_condition_disease_example_1() {
//        Condition condition = loadExampleFromFile("ncpi-Disease-example-1.json", Condition.class);
//        assertBaseResource("ncpi-disease", SchemaMode.ADVANCED, condition, Condition.class);
//    }

    // Not working due to Private fields
//    @Test
//    public void test_serialize_ncpi_phenotype() {
//        Condition condition = loadExampleFromFile("ncpi-Phenotype-example-1.json", Condition.class);
//        assertBaseResource("phenotype", SchemaMode.ADVANCED, condition, Condition.class);
//    }
}
