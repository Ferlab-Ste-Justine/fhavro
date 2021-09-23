package bio.ferlab.fhir.schema.utils;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
}
