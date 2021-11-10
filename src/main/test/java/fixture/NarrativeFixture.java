package fixture;

import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

public class NarrativeFixture {

    public static Narrative createNarrative() {
        return new Narrative().setStatus(Narrative.NarrativeStatus.GENERATED).setDiv(new XhtmlNode().setValue("xhtml"));
    }
}
