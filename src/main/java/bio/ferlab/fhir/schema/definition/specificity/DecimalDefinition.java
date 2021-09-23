package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.JsonObject;

public class DecimalDefinition extends SpecificDefinition {

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        JsonObject logicalType = JsonObjectUtils.createLogicalType(Constant.BYTES, Constant.DECIMAL)
                .add("precision", 18)
                .add("scale", 0)
                .build();
        return JsonObjectUtils.createField(name, logicalType, required);
    }
}
