package fixture;

import org.hl7.fhir.r4.model.Address;

public class AddressFixture {

    public static Address createAddress() {
        return new Address()
                .setCity("Montreal")
                .setCountry("Canada")
                .setPostalCode("T6Q 1S4")
                .setPeriod(PeriodFixture.createOngoingPeriod());
    }
}
