package fixture;

import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Type;

import java.util.List;

public class ExtensionFixture {

    public static Extension createExtension(Type type) {
        Extension extension = new Extension();
        extension.setUrl("http://url-to-the-type");
        extension.setValue(type);
        return extension;
    }

    public static Extension createBooleanExtension(boolean value) {
        Extension extension = new Extension();
        extension.setUrl("http://url-to-another-value");
        extension.setValue(new BooleanType(value));
        return extension;
    }

    public static List<Extension> createExtensions() {
        return List.of(createBooleanExtension(false), createBooleanExtension(true));
    }
}
