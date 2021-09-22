package bio.ferlab.fhir.schema.definition;

import javax.json.JsonObject;

public interface IDefinition {

    JsonObject convertToJson(String root, String name, boolean required);
}
