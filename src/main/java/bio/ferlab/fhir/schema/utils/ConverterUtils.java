package bio.ferlab.fhir.schema.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.text.WordUtils;

public class ConverterUtils {

    private ConverterUtils() {}

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

    public static String capitalizeWord(String word) {
        return WordUtils.capitalize(word);
    }
}
