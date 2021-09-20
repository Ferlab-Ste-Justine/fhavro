package fixture;

import org.hl7.fhir.r4.model.Quantity;

public class QuantityFixture {

    public static Quantity createQuantity() {
        return new Quantity()
                .setValue(4.12)
                .setUnit("10 trillion/L")
                .setSystem("http://unitsofmeasure.org")
                .setCode("10*12/L");
    }
}
