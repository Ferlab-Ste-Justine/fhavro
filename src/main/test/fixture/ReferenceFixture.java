package fixture;

import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;

public class ReferenceFixture {

    public static Reference createAbsoluteReference() {
        return new Reference()
                .setReference("http://absolute-url")
                .setType("Patient")
                .setDisplay("Absolute Reference");
    }
}
