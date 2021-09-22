package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class ReferenceDefinition extends SpecificDefinition {

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        JsonArray fields = Json.createArrayBuilder()
                .add(JsonObjectUtils.createConst("reference", Constant.STRING, false))
                .add(JsonObjectUtils.createConst("type", Constant.STRING, false))
                .add(JsonObjectUtils.createConst("identifier", Constant.STRING, false))
                .add(JsonObjectUtils.createConst("display", Constant.STRING, false))
                .build();
        return JsonObjectUtils.createInnerRecord(name, name, "A Reference", fields, required);
    }
}
