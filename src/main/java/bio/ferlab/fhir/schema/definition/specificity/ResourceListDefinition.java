package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.utils.Constant;

import javax.json.Json;
import javax.json.JsonObject;

public class ResourceListDefinition extends SpecificDefinition {

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        return Json.createObjectBuilder()
                .add(Constant.NAME, getIdentifier().toLowerCase())
                .add(Constant.TYPE, Constant.STRING)
                .build();
    }
}
