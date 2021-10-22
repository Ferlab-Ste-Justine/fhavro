package bio.ferlab.fhir.schema.repository;

import bio.ferlab.fhir.converter.Operation;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.definition.*;
import bio.ferlab.fhir.schema.definition.exception.UnknownDefinitionException;
import bio.ferlab.fhir.schema.definition.specificity.ExtensionDefinition;
import bio.ferlab.fhir.schema.definition.specificity.SpecificDefinition;
import bio.ferlab.fhir.schema.definition.specificity.SpecificDefinitionFactory;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.SchemaUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.json.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DefinitionRepository {

    private static final Map<String, PrimitiveDefinition> primitiveDefinitions = new HashMap<>();
    private static final Map<String, ComplexDefinition> complexDefinitions = new HashMap<>();
    private static final Map<String, IDefinition> specificDefinitions = new HashMap<>();
    private static final Map<String, List<String>> definedRecords = new HashMap<>();

    private static final List<Map<String, ? extends IDefinition>> definitions = List.of(
            specificDefinitions,
            complexDefinitions,
            primitiveDefinitions);

    private static SchemaMode schemaMode;

    private static Integer recursivityDepth = 0;

    private DefinitionRepository() {
    }

    public static void initialize(SchemaMode schemaMode) {
        InputStream resource = ClassLoader.getSystemClassLoader().getResourceAsStream("fhir.schema.json");
        if (resource == null) {
            throw new IllegalArgumentException("The source file fhir.schema.json is not found!");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(resource);

            if (DefinitionRepository.getSchemaMode() != schemaMode) {
                setSchemaMode(schemaMode);
                populatePrimitiveDefinitions(root);
                populateComplexDefinitions(root);
            }

            ExtensionDefinition.initializeExtensions(schemaMode);
        } catch (IOException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    public static Operation<BaseDefinition> generateDefinition(SchemaDefinition schemaDefinition) {
        ComplexDefinition complexDefinition = DefinitionRepository.getComplexDefinitionByIdentifier(schemaDefinition.getSchemaName());

        // Generate the properties, apply the profile if any.
        complexDefinition.generateProperties(schemaDefinition);
        if (complexDefinition.getDefinition().get("properties").has("resourceType")) {

            // This method will generate the internal Json necessary.
            complexDefinition.convertToJson(schemaDefinition.getSchemaName(), schemaDefinition.getSchemaName(), true);

            definedRecords.clear();

            return new Operation<>(complexDefinition);
        }
        return new Operation<>();
    }

    public static ComplexDefinition getComplexDefinitionByIdentifier(String identifier) {
        return Optional.ofNullable(complexDefinitions.get(identifier)).orElseThrow(() -> new UnknownDefinitionException(identifier));
    }

    public static PrimitiveDefinition getPrimitiveDefinitionByIdentifier(String identifier) {
        return Optional.ofNullable(primitiveDefinitions.get(identifier)).orElseThrow(() -> new UnknownDefinitionException(identifier));
    }

    public static IDefinition getSpecificDefinitionByIdentifier(String identifier) {
        return Optional.ofNullable(specificDefinitions.get(identifier)).orElseThrow(() -> new UnknownDefinitionException(identifier));
    }

    public static IDefinition getDefinitionByIdentifier(String identifier) {
        return definitions.stream()
                .filter(map -> map.containsKey(identifier))
                .findFirst()
                .orElseThrow(() -> new UnknownDefinitionException(identifier))
                .get(identifier);
    }

    public static JsonObject getReferenceObject(String root, JsonNode node, String name, boolean required) {
        return getDefinitionByIdentifier(SchemaUtils.parseReference(node))
                .convertToJson(root, name, required);
    }

    public static boolean registerInnerRecords(String root, String innerRecord) {
        if (definedRecords.containsKey(root)) {
            if (definedRecords.get(root).contains(innerRecord)) {
                return true;
            }

            definedRecords.get(root).add(innerRecord);
        } else {
            List<String> innerRecords = new ArrayList<>();
            innerRecords.add(innerRecord);
            definedRecords.put(root, innerRecords);
        }
        return false;
    }

    public static Map<String, ComplexDefinition> getComplexDefinitions() {
        return complexDefinitions;
    }

    public static SchemaMode getSchemaMode() {
        return DefinitionRepository.schemaMode;
    }

    public static Integer getRecursivityDepth() {
        return recursivityDepth;
    }

    public static void setRecursivityDepth(Integer recursivityDepth) {
        DefinitionRepository.recursivityDepth = recursivityDepth;
    }

    private static void populateComplexDefinitions(JsonNode root) {
        for (Iterator<Map.Entry<String, JsonNode>> it = root.get("discriminator").get("mapping").fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            ComplexDefinition complexDefinition = new ComplexDefinition(entry, root.get(Constant.DEFINITIONS).get(entry.getKey()));
            complexDefinitions.put(complexDefinition.getIdentifier(), complexDefinition);
        }
    }

    private static void populatePrimitiveDefinitions(JsonNode root) {
        for (Iterator<Map.Entry<String, JsonNode>> it = root.get(Constant.DEFINITIONS).fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();

            if (SpecificDefinitionFactory.isSupported(entry.getKey())) {
                SpecificDefinition specificDefinition = SpecificDefinitionFactory.getSpecificDefinition(entry.getKey());
                specificDefinition.initialize(entry.getKey(), entry.getKey(), entry.getValue());
                specificDefinitions.put(specificDefinition.getIdentifier(), specificDefinition);
                continue;
            }

            if (entry.getValue().has(Constant.TYPE)) {
                PrimitiveDefinition primitiveDefinition = new PrimitiveDefinition(entry);
                primitiveDefinitions.put(primitiveDefinition.getIdentifier(), primitiveDefinition);
                continue;
            }

            if ("Element".equalsIgnoreCase(entry.getKey())) {
                continue;
            }

            if (entry.getValue().has(Constant.PROPERTIES)) {
                ComplexDefinition complexDefinition = new ComplexDefinition(entry, entry.getValue());
                complexDefinition.generateProperties();
                if (!complexDefinition.getProperties().isEmpty()) {
                    complexDefinitions.put(complexDefinition.getIdentifier(), complexDefinition);
                }
            }
        }
    }

    private static void setSchemaMode(SchemaMode schemaMode) {
        DefinitionRepository.schemaMode = schemaMode;
    }
}
