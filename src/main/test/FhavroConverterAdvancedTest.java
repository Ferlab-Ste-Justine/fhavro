import bio.ferlab.fhir.schema.repository.SchemaMode;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;

import java.util.List;

public class FhavroConverterAdvancedTest extends BaseFhavroConverter {

    @Test
    public void test_serialize_ncpi_family_relationship() {
        List<String> examples = List.of("ncpi-FamilyRelationship-example-1.json", "ncpi-FamilyRelationship-example-2.json", "ncpi-FamilyRelationship-example-3.json", "ncpi-FamilyRelationship-example-4.json");
        for (String example : examples) {
            assertBaseResource("ncpi-family-relationship", SchemaMode.ADVANCED, loadExampleFromFile(example, Observation.class), Observation.class);
        }
    }

    @Test
    public void test_serialize_cqdg_patient_examples() {
        List<String> examples = List.of("cqdg-Patient-example-1.json", "cqdg-Patient-example-2.json");
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

    @Test
    public void test_serialize_ncpi_document_reference() {
        DocumentReference documentReference = loadExampleFromFile("ncpi-DocumentReference-example-1.json", DocumentReference.class);
        assertBaseResource("drsdocumentreference", SchemaMode.ADVANCED, documentReference, DocumentReference.class);
    }

    @Test
    public void test_serialize_ncpi_condition_disease_example_2() {
        Condition condition = loadExampleFromFile("ncpi-Disease-example-2.json", Condition.class);
        assertBaseResource("ncpi-disease", SchemaMode.ADVANCED, condition, Condition.class);
    }

    @Test
    public void test_serialize_kfdrc_condition_example_1() {
        Condition condition = loadExampleFromFile("kfdrc-Condition-example-1.json", Condition.class);
        assertBaseResource("kfdrc-condition", SchemaMode.ADVANCED, condition, Condition.class);
    }

    @Test
    public void test_serialize_kfdrc_observation_example_1() {
        Observation observation = loadExampleFromFile("kfdrc-Observation-example-1.json", Observation.class);
        assertBaseResource("kfdrc-observation", SchemaMode.ADVANCED, observation, Observation.class);
    }

    @Test
    public void test_serialize_ncpi_Specimen_example_1() {
        Specimen specimen = loadExampleFromFile("ncpi-Specimen-example-1.json", Specimen.class);
        assertBaseResource("ncpi-specimen", SchemaMode.ADVANCED, specimen, Specimen.class);
    }

    @Test
    public void test_serialize_ncpi_condition_disease_example_1() {
        Condition condition = loadExampleFromFile("ncpi-Disease-example-1.json", Condition.class);
        assertBaseResource("ncpi-disease", SchemaMode.ADVANCED, condition, Condition.class);
    }

    @Test
    public void test_serialize_ncpi_phenotype() {
        Condition condition = loadExampleFromFile("ncpi-Phenotype-example-1.json", Condition.class);
        assertBaseResource("ncpi-phenotype", SchemaMode.ADVANCED, condition, Condition.class);
    }
}
