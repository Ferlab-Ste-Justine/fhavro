package fixture;

import org.hl7.fhir.r4.model.EffectEvidenceSynthesis;

import java.util.Date;

public class EffectEvidenceSynthesisFixture {

    public static EffectEvidenceSynthesis createEffectEvidenceSynthesis() {
        EffectEvidenceSynthesis effectEvidenceSynthesis = new EffectEvidenceSynthesis();
        effectEvidenceSynthesis.setApprovalDate(new Date());
        effectEvidenceSynthesis.setCopyright("Copyright");
        effectEvidenceSynthesis.setExposure(ReferenceFixture.createAbsoluteReference());
        effectEvidenceSynthesis.setDescription("This is a fixture to mostly test the #/definitions/date");
        return effectEvidenceSynthesis;
    }
}
