package bio.ferlab.fhir.schema.definition;

import com.fasterxml.jackson.databind.JsonNode;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.JsonObject;
import java.util.Map;

public class PrimitiveDefinition extends BaseDefinition {

    private String type;
    private String pattern;

    public PrimitiveDefinition(Map.Entry<String, JsonNode> entry) {
        super(entry.getKey(), entry.getKey(), entry.getValue());
        if (getDefinition().get(Constant.TYPE).asText().equals("number")) {
            setType("int");
        } else {
            setType(getDefinition().get(Constant.TYPE).asText());
        }

        if (getDefinition().has(Constant.PATTERN)) {
            setPattern(getDefinition().get(Constant.PATTERN).asText());
        }
    }

    public JsonObject convertToJson(String root, String name, boolean required) {
        return JsonObjectUtils.createConst(name, getType(), required);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }
}
