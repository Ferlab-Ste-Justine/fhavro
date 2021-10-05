package fixture;

import org.hl7.fhir.r4.model.Identifier;

public class IdentifierFixture {

    public static Identifier createIdentifier() {
        return new Identifier()
                .setPeriod(PeriodFixture.createPlannedPeriod())
                .setSystem("http://this-is-the-best-system-ever")
                .setUse(Identifier.IdentifierUse.OFFICIAL)
                .setAssigner(ReferenceFixture.createAbsoluteReference())
                .setType(CodeableConceptFixture.createCodeableConcept())
                .setValue("IdentifierValue");
    }

    public static Identifier createDifferentIdentifier() {
        return new Identifier()
                .setPeriod(PeriodFixture.createOngoingPeriod())
                .setSystem("http://this-is-the-second-best-system-ever")
                .setUse(Identifier.IdentifierUse.SECONDARY)
                .setAssigner(ReferenceFixture.createAbsoluteReference())
                .setType(CodeableConceptFixture.createCodeableConcept())
                .setValue("IdentifierValueTest");
    }
}
