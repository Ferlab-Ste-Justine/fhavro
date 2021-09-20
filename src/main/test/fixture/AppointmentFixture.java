package fixture;

import org.hl7.fhir.r4.model.Appointment;

public class AppointmentFixture {

    public static Appointment createAppointment() {
        return new Appointment()
                .addIdentifier(IdentifierFixture.createIdentifier())
                .addBasedOn(ReferenceFixture.createAbsoluteReference())
                .addRequestedPeriod(PeriodFixture.createPlannedPeriod())
                .addReasonCode(CodeableConceptFixture.createCodeableConcept());
    }
}
