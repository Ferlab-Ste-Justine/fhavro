package bio.ferlab.fhir.schema.parser.regex;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.definition.specificity.DateTimeDefinition;

import javax.json.JsonObject;

public class DateTimeParser extends RegexParser {

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        return new DateTimeDefinition().convertToJson(root, identifier, property.isRequired());
    }

    @Override
    protected String getReferencePattern() {
        return "2015-02-07T13:28:17Z";
    }
}
