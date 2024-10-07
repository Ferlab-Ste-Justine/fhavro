import bio.ferlab.fhir.schema.repository.SchemaMode;
import org.hl7.fhir.r4.model.DocumentReference;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.ResearchStudy;
import org.hl7.fhir.r4.model.ResearchSubject;
import org.junit.jupiter.api.Test;

import java.util.List;

public class FhavroListTest extends BaseFhavro {

    @Test
    public void test_serialize_kfdrc_patient_examples_all() {
        List<String> examples = List.of("kfdrc-Patient-examples-1.json", "kfdrc-Patient-examples-2.json", "kfdrc-Patient-examples-3.json");
        for (String example : examples) {
            List<Patient> patients = loadExamplesFromFile(example, Patient.class);
            for (Patient patient : patients) {
                assertBaseResource("schema/kfdrc-patient", SchemaMode.ADVANCED, patient, Patient.class);
            }
        }
    }

    @Test
    public void test_serialize_include_research_subject_examples() {
        List<String> examples = List.of("kfdrc-ResearchSubject-examples-1.json", "kfdrc-ResearchSubject-examples-2.json", "kfdrc-ResearchSubject-examples-3.json");
        for (String example : examples) {
            List<ResearchSubject> researchSubjects = loadExamplesFromFile(example, ResearchSubject.class);
            for (ResearchSubject researchSubject : researchSubjects) {
                assertBaseResource("schema/kfdrc-researchsubject", SchemaMode.ADVANCED, researchSubject, ResearchSubject.class);
            }
        }
    }

    @Test
    public void test_serialize_include_document_reference_examples() {
        List<String> examples = List.of("kfdrc-DocumentReference-examples-1.json", "kfdrc-DocumentReference-examples-2.json", "kfdrc-DocumentReference-examples-3.json", "kfdrc-DocumentReference-examples-4.json", "kfdrc-DocumentReference-examples-5.json");
        for (String example : examples) {
            List<DocumentReference> documentReferences = loadExamplesFromFile(example, DocumentReference.class);
            for (DocumentReference documentReference : documentReferences) {
                assertBaseResource("schema/drsdocumentreference", SchemaMode.ADVANCED, documentReference, DocumentReference.class);
            }
        }
    }

    @Test
    public void test_serialize_kfdrc_researchStudy_examples() {
        List<String> examples = List.of("kfdrc-ResearchStudy-examples-1.json");
        for (String example : examples) {
            List<ResearchStudy> researchStudies = loadExamplesFromFile(example, ResearchStudy.class);
            for (ResearchStudy researchStudy : researchStudies) {
                assertBaseResource("schema/kfdrc-researchstudy", SchemaMode.ADVANCED, researchStudy, ResearchStudy.class);
            }
        }
    }
}
