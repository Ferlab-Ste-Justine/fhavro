package bio.ferlab.fhir.schema.definition.specificity;

import com.fasterxml.jackson.databind.JsonNode;
import bio.ferlab.fhir.schema.definition.BaseDefinition;

public abstract class SpecificDefinition extends BaseDefinition {

    protected SpecificDefinition() {}

    public void initialize(String name, String identifier, JsonNode definition) {
        setName(name);
        setIdentifier(identifier);
        setDefinition(definition);
    }
}
