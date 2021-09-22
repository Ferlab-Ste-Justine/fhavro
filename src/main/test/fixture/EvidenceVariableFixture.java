package fixture;

import org.hl7.fhir.r4.model.EvidenceVariable;

import java.util.Date;

public class EvidenceVariableFixture {

    public static EvidenceVariable createEvidenceVariable() {
        EvidenceVariable evidenceVariable = new EvidenceVariable();
        evidenceVariable.setApprovalDate(new Date());
        return evidenceVariable;
    }
}
