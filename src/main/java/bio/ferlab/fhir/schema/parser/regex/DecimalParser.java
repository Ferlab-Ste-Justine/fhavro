package bio.ferlab.fhir.schema.parser.regex;

import bio.ferlab.fhir.schema.definition.Property;

import javax.json.JsonObject;

public class DecimalParser extends RegexParser {

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        return new DecimalParser().parseField(root, identifier, property);
    }

    // Must supported minimally 18 decimal digits; https://www.w3.org/TR/xmlschema-2/#decimal
    @Override
    protected String getReferencePattern() {
        return "0.000000000000000000";
    }
}
