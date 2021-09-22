package fixture;

import org.hl7.fhir.r4.model.EventDefinition;

import java.util.Date;

public class EventDefinitionFixture {

    public static EventDefinition createEventDefinition() {
        EventDefinition eventDefinition = new EventDefinition();
        eventDefinition.setApprovalDate(new Date());
        return eventDefinition;
    }
}
