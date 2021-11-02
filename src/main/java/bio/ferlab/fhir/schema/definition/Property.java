package bio.ferlab.fhir.schema.definition;

import com.fasterxml.jackson.databind.JsonNode;

public class Property {

    private JsonNode jsonNode;
    private boolean required;

    public Property(JsonNode jsonNode) {
        setJsonNode(jsonNode);
        setRequired(false);
    }

    public Property(JsonNode jsonNode, boolean required) {
        setJsonNode(jsonNode);
        setRequired(required);
    }

    public JsonNode getJsonNode() {
        return jsonNode;
    }

    public void setJsonNode(JsonNode jsonNode) {
        this.jsonNode = jsonNode;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
