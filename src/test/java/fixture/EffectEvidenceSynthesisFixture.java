package fixture;

import org.hl7.fhir.r4.model.EffectEvidenceSynthesis;
import org.hl7.fhir.r4.model.Range;
import org.hl7.fhir.r4.model.UsageContext;

import java.util.Date;
import java.util.List;

public class EffectEvidenceSynthesisFixture {

    public static EffectEvidenceSynthesis createEffectEvidenceSynthesis() {
        EffectEvidenceSynthesis effectEvidenceSynthesis = new EffectEvidenceSynthesis();
        effectEvidenceSynthesis.setApprovalDate(new Date());
        effectEvidenceSynthesis.setCopyright("Copyright");
        effectEvidenceSynthesis.setExposure(ReferenceFixture.createAbsoluteReference());
        effectEvidenceSynthesis.setDescription("This is a fixture to mostly test the #/definitions/date");
        UsageContext usageContext = new UsageContext();
        Range range = new Range();
        range.setHigh(QuantityFixture.createQuantity());
        range.setLow(QuantityFixture.createQuantity());
        usageContext.setValue(range);

        effectEvidenceSynthesis.setUseContext(List.of(usageContext));
        effectEvidenceSynthesis.setText(NarrativeFixture.createNarrative());
        return effectEvidenceSynthesis;
    }
}
