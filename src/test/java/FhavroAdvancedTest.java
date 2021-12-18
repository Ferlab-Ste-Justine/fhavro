import bio.ferlab.fhir.Fhavro;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import fixture.PatientFixture;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;

import java.util.List;

public class FhavroAdvancedTest extends BaseFhavro {

    @Test
    public void test_serialize_ncpi_family_relationship() {
        List<String> examples = List.of("ncpi-FamilyRelationship-example-1.json", "ncpi-FamilyRelationship-example-2.json", "ncpi-FamilyRelationship-example-3.json", "ncpi-FamilyRelationship-example-4.json");
        for (String example : examples) {
            assertBaseResource("schema/ncpi-family-relationship", SchemaMode.ADVANCED, loadExampleFromFile(example, Observation.class), Observation.class);
        }
    }

    @Test
    public void test_serialize_cqdg_patient_examples() {
        List<String> examples = List.of("cqdg-Patient-example-1.json", "cqdg-Patient-example-2.json");
        for (String example : examples) {
            assertBaseResource("schema/cqgc-patient", SchemaMode.ADVANCED, loadExampleFromFile(example, Patient.class), Patient.class);
        }
    }

    @Test
    public void test_serialize_kfdrc_patient_examples() {
        List<String> examples = List.of("kfdrc-Patient-example-1.json");
        for (String example : examples) {
            assertBaseResource("schema/kfdrc-patient", SchemaMode.ADVANCED, loadExampleFromFile(example, Patient.class), Patient.class);
        }
    }

    @Test
    public void test_serialize_ncpi_document_reference() {
        DocumentReference documentReference = loadExampleFromFile("ncpi-DocumentReference-example-1.json", DocumentReference.class);
        assertBaseResource("schema/drsdocumentreference", SchemaMode.ADVANCED, documentReference, DocumentReference.class);
    }

    @Test
    public void test_serialize_ncpi_condition_disease_example_2() {
        Condition condition = loadExampleFromFile("ncpi-Disease-example-2.json", Condition.class);
        assertBaseResource("schema/ncpi-disease", SchemaMode.ADVANCED, condition, Condition.class);
    }

    @Test
    public void test_serialize_kfdrc_condition_example_1() {
        Condition condition = loadExampleFromFile("kfdrc-Condition-example-1.json", Condition.class);
        assertBaseResource("schema/kfdrc-condition", SchemaMode.ADVANCED, condition, Condition.class);
    }

    @Test
    public void test_serialize_kfdrc_observation_example_1() {
        Observation observation = loadExampleFromFile("kfdrc-Observation-example-1.json", Observation.class);
        assertBaseResource("schema/kfdrc-observation", SchemaMode.ADVANCED, observation, Observation.class);
    }

    @Test
    public void test_serialize_ncpi_Specimen_example_1() {
        Specimen specimen = loadExampleFromFile("ncpi-Specimen-example-1.json", Specimen.class);
        assertBaseResource("schema/ncpi-specimen", SchemaMode.ADVANCED, specimen, Specimen.class);
    }

    @Test
    public void test_serialize_ncpi_condition_disease_example_1() {
        Condition condition = loadExampleFromFile("ncpi-Disease-example-1.json", Condition.class);
        assertBaseResource("schema/ncpi-disease", SchemaMode.ADVANCED, condition, Condition.class);
    }

    @Test
    public void test_serialize_ncpi_phenotype() {
        Condition condition = loadExampleFromFile("ncpi-Phenotype-example-1.json", Condition.class);
        assertBaseResource("schema/ncpi-phenotype", SchemaMode.ADVANCED, condition, Condition.class);
    }


    @Test
    public void test_serialize_patient_with_extension_with_simple_schema() {
        Patient patient = loadExampleFromFile("myext.json", Patient.class);
        /*
         "_maritalStatus": {
    "extension": [
      {
        "url": "http://hl7.org/maritalSimple",
        "valueString": "married"
      },
      {
        "url": "http://hl7.org/maritalComplex",
        "extension": [
          {
            "url": "since",
            "valueInteger": 10
          },
          {
            "url": "with",
            "valueString": "Someone"
          }
        ]

      }
    ]
  },
         */

        Schema schema = Fhavro.loadSchema("patient_with_ext2.avsc", SchemaMode.SIMPLE);
        GenericRecord input = Fhavro.convertResourceToGenericRecord(patient, schema);
        Patient p = Fhavro.convertGenericRecordToResource(input, schema, "Patient" );
        System.out.println(input);
    }

    @Test
    public void test_serialize__cpi_document_references_with_ext() {
        DocumentReference patient = loadExampleFromFile("ncpi-DocumentReference-example-1.json", DocumentReference.class);
        Schema schema = Fhavro.loadSchema("ncpi-documentreference.avsc", SchemaMode.SIMPLE);
        GenericRecord input = Fhavro.convertResourceToGenericRecord(patient, schema);
        DocumentReference dr = Fhavro.convertGenericRecordToResource(input, schema, "DocumentReference" );
        System.out.println(input);
    }
}
