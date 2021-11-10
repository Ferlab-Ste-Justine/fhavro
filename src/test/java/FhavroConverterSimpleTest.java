import bio.ferlab.fhir.schema.repository.SchemaMode;
import fixture.*;
import org.hl7.fhir.r4.model.*;
import org.junit.Test;

public class FhavroConverterSimpleTest extends BaseFhavroConverter {

    @Test
    public void test_serialize_patient_with_simple_schema() {
        assertBaseResource("Patient", SchemaMode.SIMPLE, PatientFixture.createSimplePatient(), Patient.class);
    }

    @Test
    public void test_serialize_appointment_with_simple_schema() {
        assertBaseResource("Appointment", SchemaMode.SIMPLE, AppointmentFixture.createSimpleAppointment(), Appointment.class);
    }

    @Test
    public void test_serialize_account_with_simple_schema() {
        assertBaseResource("Account", SchemaMode.SIMPLE, AccountFixture.createSimpleAccount(), Account.class);
    }

    @Test
    public void test_serialize_eventDefinition_with_simple_schema() {
        assertBaseResource("EventDefinition", SchemaMode.SIMPLE, EventDefinitionFixture.createSimpleEventDefinition(), EventDefinition.class);
    }

    @Test
    public void test_serialize_evidenceVariable_with_simple_schema() {
        assertBaseResource("EvidenceVariable", SchemaMode.SIMPLE, EvidenceVariableFixture.createSimpleEvidenceVariable(), EvidenceVariable.class);
    }
}
