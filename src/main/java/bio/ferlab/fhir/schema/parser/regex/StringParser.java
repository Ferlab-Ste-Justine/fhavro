package bio.ferlab.fhir.schema.parser.regex;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.JsonObject;

public class StringParser extends RegexParser {

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        return JsonObjectUtils.createConst(identifier, Constant.STRING, property.isRequired());
    }

    @Override
    protected String getReferencePattern() {
        return "This is a string!";
    }
}
