package fixture;

import org.hl7.fhir.r4.model.Reference;

public class ReferenceFixture {

    public static Reference createAbsoluteReference() {
        return new Reference()
                .setReference("http://absolute-url")
                .setType("Patient")
                .setDisplay("Absolute Reference");
    }

    public static Reference createRelativeReference() {
        return new Reference()
                .setReference("http://relative-url")
                .setType("Patient")
                .setDisplay("Relative Reference")
                .setIdentifier(IdentifierFixture.createIdentifier());
    }
}
