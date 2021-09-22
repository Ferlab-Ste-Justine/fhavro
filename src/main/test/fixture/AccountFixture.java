package fixture;

import org.hl7.fhir.r4.model.Account;

public class AccountFixture {

    public static Account createAccount() {
        return new Account()
                .setServicePeriod(PeriodFixture.createOngoingPeriod())
                .addIdentifier(IdentifierFixture.createIdentifier())
                .addSubject(ReferenceFixture.createAbsoluteReference())
                .setName("Name")
                .setType(CodeableConceptFixture.createCodeableConcept());
    }
}
