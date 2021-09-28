package fixture;

import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Type;

public class ExtensionFixture {

    public static Extension createExtension(Type type) {
        Extension extension = new Extension();
        extension.setUrl("http://url-to-the-type");
        extension.setValue(type);
        return extension;
    }
}
