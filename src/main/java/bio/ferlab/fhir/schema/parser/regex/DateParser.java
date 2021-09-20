package bio.ferlab.fhir.schema.parser.regex;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.definition.specificity.DateDefinition;

import javax.json.JsonObject;

public class DateParser extends RegexParser {

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        return new DateDefinition().convertToJson(root, identifier, property.isRequired());
    }

    @Override
    protected String getReferencePattern() {
        return "1970-01-01";
    }
}
