package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class XHtmlDefinition extends SpecificDefinition {

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        JsonArray fields = Json.createArrayBuilder()
                .add(JsonObjectUtils.createConst(Constant.EXTENSION, Constant.STRING, false))
                .add(JsonObjectUtils.createConst("url", Constant.STRING, false))
                .add(JsonObjectUtils.createConst("valueString", Constant.STRING, false))
                .build();
        return JsonObjectUtils.createInnerRecord(name, name, "", fields, required);
    }
}
