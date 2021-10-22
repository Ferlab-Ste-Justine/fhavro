package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.JsonObject;

public class XHtmlDefinition extends SpecificDefinition {

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        return JsonObjectUtils.createConst(name, Constant.STRING, false);
    }
}
