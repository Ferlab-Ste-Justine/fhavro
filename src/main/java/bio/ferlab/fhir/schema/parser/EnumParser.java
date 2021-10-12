package bio.ferlab.fhir.schema.parser;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.JsonObject;

public class EnumParser implements IParser {

    @Override
    public boolean canParse(Property property) {
        return property.getJsonNode().has(Constant.ENUM);
    }

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        return JsonObjectUtils.createConst(identifier, Constant.STRING, property.isRequired());
    }
}
