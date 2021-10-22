package fixture;

import org.hl7.fhir.r4.model.EvidenceVariable;
import org.hl7.fhir.r4.model.MarkdownType;

import java.util.Date;
import java.util.List;

public class EvidenceVariableFixture {

    public static EvidenceVariable createEvidenceVariable() {
        EvidenceVariable evidenceVariable = new EvidenceVariable();
        evidenceVariable.setApprovalDate(new Date());
        evidenceVariable.setText(NarrativeFixture.createNarrative());
        evidenceVariable.setIdentifier(List.of(IdentifierFixture.createIdentifier(), IdentifierFixture.createIdentifier()));
        evidenceVariable.setTitle("EVIDENCE YOUR HONOR!");
        evidenceVariable.setCopyright("The Copyright.");
        evidenceVariable.setExtension(List.of(ExtensionFixture.createExtension(new MarkdownType("Code"))));
        return evidenceVariable;
    }

    public static EvidenceVariable createSimpleEvidenceVariable() {
        EvidenceVariable evidenceVariable = createEvidenceVariable();
        evidenceVariable.setExtension(null);
        return evidenceVariable;
    }
}
