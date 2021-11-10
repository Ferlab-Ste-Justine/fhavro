package fixture;

import org.hl7.fhir.r4.model.Account;

public class AccountFixture {

    public static Account createAccount() {
        Account account = new Account()
                .setServicePeriod(PeriodFixture.createOngoingPeriod())
                .addIdentifier(IdentifierFixture.createIdentifier())
                .addSubject(ReferenceFixture.createAbsoluteReference())
                .setStatus(Account.AccountStatus.ENTEREDINERROR)
                .setName("Name")
                .setType(CodeableConceptFixture.createCodeableConcept());
        account.setText(NarrativeFixture.createNarrative());
        return account;
    }

    public static Account createSimpleAccount() {
        Account account = createAccount();
        account.getExtension().clear();
        return account;
    }
}
