package bio.ferlab.fhir.schema.utils;

import com.fasterxml.jackson.databind.JsonNode;

public class SchemaUtils {

    private SchemaUtils() {}

    public static String parseReference(JsonNode jsonNode) {
        return jsonNode.get(Constant.REF).toString()
                .replace("\"", "")
                .replace("#/definitions/", "");
    }

    public static String parsePrimitiveType(String type) {
        type = type.replace("\"", "");
        if ("number".equals(type)) {
            return "integer";
        }
        return type;
    }

    public static String parseBaseDefinition(String baseDefinition) {
        return baseDefinition.substring(baseDefinition.lastIndexOf('/') + 1).trim();
    }

    public static String parsePropertyName(String propertyName) {
        return propertyName.substring(propertyName.lastIndexOf('.') + 1).trim();
    }
}
