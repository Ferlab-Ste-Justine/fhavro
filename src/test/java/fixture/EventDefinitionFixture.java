package fixture;

import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.EventDefinition;

import java.util.Date;
import java.util.List;

public class EventDefinitionFixture {

    public static EventDefinition createEventDefinition() {
        EventDefinition eventDefinition = new EventDefinition();
        eventDefinition.setApprovalDate(new Date());
        eventDefinition.setCopyright("Copyright");
        eventDefinition.setStatus(Enumerations.PublicationStatus.RETIRED);
        eventDefinition.setText(NarrativeFixture.createNarrative());
        eventDefinition.setExtension(List.of(ExtensionFixture.createExtension(new DateTimeType(new Date()))));
        return eventDefinition;
    }

    public static EventDefinition createSimpleEventDefinition() {
        EventDefinition eventDefinition = createEventDefinition();
        eventDefinition.setExtension(null);
        return eventDefinition;
    }
}
