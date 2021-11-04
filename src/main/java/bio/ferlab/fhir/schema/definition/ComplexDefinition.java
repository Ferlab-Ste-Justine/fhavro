package bio.ferlab.fhir.schema.definition;

import bio.ferlab.fhir.schema.parser.ParserServant;
import bio.ferlab.fhir.schema.profile.Profiler;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;
import com.fasterxml.jackson.databind.JsonNode;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.*;

public class ComplexDefinition extends BaseDefinition {

    private static final List<String> UNSUPPORTED_PROPERTIES = List.of("extension", "modifierExtension");

    private final LinkedHashMap<String, Property> properties = new LinkedHashMap<>();
    private final List<String> requiredProperties = new ArrayList<>();

    public ComplexDefinition(Map.Entry<String, JsonNode> entry, JsonNode definition) {
        super(entry.getKey(), entry.getKey(), entry.getValue());
        setDefinition(definition);
    }

    public void generateProperties() {
        generateProperties(new SchemaDefinition());
    }

    public void generateProperties(SchemaDefinition schemaDefinition) {
        if (getDefinition().has(Constant.REQUIRED)) {
            getDefinition().get(Constant.REQUIRED).forEach(x -> requiredProperties.add(x.toString().replace("\"", "")));
        }

        if (getDefinition().has(Constant.DESCRIPTION)) {
            setDescription(getDefinition().get(Constant.DESCRIPTION).asText());
        }

        SchemaMode schemaMode = DefinitionRepository.getSchemaMode();

        // Ensure that the Extension properties is the first one to be populated.
        if ((schemaMode == SchemaMode.DEFAULT || schemaMode == SchemaMode.ADVANCED) && getDefinition().get(Constant.PROPERTIES).has(Constant.EXTENSION)) {
            properties.put(Constant.EXTENSION, new Property(getDefinition().get(Constant.PROPERTIES).get(Constant.EXTENSION), false));
        }

        for (Iterator<Map.Entry<String, JsonNode>> it = getDefinition().get(Constant.PROPERTIES).fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> node = it.next();

            // "_" field are not supported by default, they need to be manually added using Profile.
            if (node.getKey().contains("_") || UNSUPPORTED_PROPERTIES.contains(node.getKey())) {
                continue;
            }

            properties.put(node.getKey(), new Property(node.getValue(), requiredProperties.contains(node.getKey())));
        }

        if (schemaDefinition.hasProfile()) {
            Profiler.applyProfile(this, schemaDefinition, requiredProperties, properties);
        }
    }

    public JsonObject convertToJson(String root, String name, boolean required) {
        if (properties.isEmpty()) {
            generateProperties();
        }

        JsonArrayBuilder fields = Json.createArrayBuilder();
        for (Map.Entry<String, Property> node : properties.entrySet()) {
            fields.add(ParserServant.parseField(root, node.getKey(), node.getValue()));
        }

        if (root.equalsIgnoreCase(getName())) {
            setJsonObject(JsonObjectUtils.createRecord(name, getDescription(), fields.build(), false));
        } else {
            if (DefinitionRepository.registerInnerRecords(root, getName())) {
                setJsonObject(JsonObjectUtils.createRedefinedRecord(name, getName(), Json.createObjectBuilder().build()));
            } else {
                setJsonObject(JsonObjectUtils.createInnerRecord(name, getName(), getDescription(), fields.build(), false));
            }
        }

        return getJsonObject();
    }

    public Map<String, Property> getProperties() {
        return properties;
    }
}
