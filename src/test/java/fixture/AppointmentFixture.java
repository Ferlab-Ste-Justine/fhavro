package fixture;

import org.hl7.fhir.r4.model.Appointment;
import org.hl7.fhir.r4.model.DateType;

import java.util.Date;

public class AppointmentFixture {

    public static Appointment createAppointment() {
        Appointment appointment = new Appointment()
                .addIdentifier(IdentifierFixture.createIdentifier())
                .addBasedOn(ReferenceFixture.createAbsoluteReference())
                .addRequestedPeriod(PeriodFixture.createPlannedPeriod())
                .addReasonCode(CodeableConceptFixture.createCodeableConcept());
        appointment.addExtension(ExtensionFixture.createExtension(new DateType(new Date())));
        appointment.setText(NarrativeFixture.createNarrative());
        return appointment;
    }

    public static Appointment createSimpleAppointment() {
        Appointment appointment = createAppointment();
        appointment.getExtension().clear();
        return appointment;
    }
}
