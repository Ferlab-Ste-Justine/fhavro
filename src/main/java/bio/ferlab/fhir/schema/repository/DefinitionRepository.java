package bio.ferlab.fhir.schema.repository;

import bio.ferlab.fhir.schema.definition.BaseDefinition;
import bio.ferlab.fhir.schema.definition.ComplexDefinition;
import bio.ferlab.fhir.schema.definition.IDefinition;
import bio.ferlab.fhir.schema.definition.PrimitiveDefinition;
import bio.ferlab.fhir.schema.definition.exception.UnknownDefinitionException;
import bio.ferlab.fhir.schema.definition.exception.UnknownReferenceException;
import bio.ferlab.fhir.schema.definition.specificity.SpecificDefinition;
import bio.ferlab.fhir.schema.definition.specificity.SpecificDefinitionFactory;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.ConverterUtils;
import com.fasterxml.jackson.databind.JsonNode;

import javax.json.JsonObject;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DefinitionRepository {

    private static final Map<String, PrimitiveDefinition> primitiveDefinitions = new HashMap<>();
    private static final Map<String, ComplexDefinition> complexDefinitions = new HashMap<>();
    private static final Map<String, IDefinition> specificDefinitions = new HashMap<>();
    private static final Map<String, List<String>> definedRecords = new HashMap<>();

    private DefinitionRepository() {
    }

    public static void populateComplexDefinitions(JsonNode root) {
        for (Iterator<Map.Entry<String, JsonNode>> it = root.get("discriminator").get("mapping").fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            ComplexDefinition complexDefinition = new ComplexDefinition(entry, root.get(Constant.DEFINITIONS).get(entry.getKey()));
            complexDefinitions.put(complexDefinition.getIdentifier(), complexDefinition);
        }
    }

    public static void populatePrimitiveDefinitions(JsonNode root) {
        for (Iterator<Map.Entry<String, JsonNode>> it = root.get(Constant.DEFINITIONS).fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();

            if (SpecificDefinitionFactory.isSupported(entry.getKey())) {
                SpecificDefinition specialDefinition = SpecificDefinitionFactory.getSpecificDefinition(entry.getKey());
                specialDefinition.initialize(entry.getKey(), entry.getKey(), entry.getValue());
                specificDefinitions.put(specialDefinition.getIdentifier(), specialDefinition);
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

    public static ComplexDefinition getComplexDefinitionByIdentifier(String identifier) {
        return Optional.ofNullable(complexDefinitions.get(identifier)).orElseThrow(() -> new UnknownDefinitionException(identifier));
    }

    public static PrimitiveDefinition getPrimitiveDefinitionByIdentifier(String identifier) {
        return Optional.ofNullable(primitiveDefinitions.get(identifier)).orElseThrow(() -> new UnknownDefinitionException(identifier));
    }

    public static JsonObject getReferenceObject(String root, JsonNode node, String name, boolean required) {
        String reference = ConverterUtils.parseReference(node);
        if (primitiveDefinitions.containsKey(reference)) {
            return primitiveDefinitions.get(reference).convertToJson(root, name, required);
        } else if (complexDefinitions.containsKey(reference)) {
            return complexDefinitions.get(reference).convertToJson(root, name, required);
        } else if (specificDefinitions.containsKey(reference)) {
            return specificDefinitions.get(reference).convertToJson(root, name, required);
        } else {
            throw new UnknownReferenceException(reference);
        }
    }

    /*
        Try to register an inner records for the root record.
        Return true, if the record is already register and therefore simply replace it by its name.
        Else, the record has to be explicitly defined.
     */
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

    public static void generateDefinition(String identifier) {
        DefinitionRepository.getComplexDefinitionByIdentifier(identifier).convertToJson(identifier, identifier, true);
        saveDefinition(DefinitionRepository.getComplexDefinitionByIdentifier(identifier));
        definedRecords.clear();
    }

    private static void saveDefinition(BaseDefinition baseDefinition) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/resources/schemas/" + baseDefinition.getName().toLowerCase() + ".avsc"))) {
            writer.write(baseDefinition.getJsonObject().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, ComplexDefinition> getComplexDefinitions() {
        return complexDefinitions;
    }
}
