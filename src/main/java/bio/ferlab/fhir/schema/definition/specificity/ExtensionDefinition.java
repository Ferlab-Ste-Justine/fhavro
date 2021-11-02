package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.converter.Operation;
import bio.ferlab.fhir.schema.definition.IDefinition;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;
import org.apache.commons.text.WordUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExtensionDefinition extends SpecificDefinition {

    private static LinkedHashMap<String, Extension> extensions;

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        Operation<RecursiveExtension> operation = checkIfRecursive(name);

        String formattedName = WordUtils.capitalize(name);

        if (!operation.isValid() && DefinitionRepository.registerInnerRecords(root, name)) {
            return JsonObjectUtils.createRedefinedRecord(name, formattedName, Json.createObjectBuilder().build());
        } else {
            JsonArrayBuilder fields = Json.createArrayBuilder();
            fields.add(JsonObjectUtils.createConst("url", "string", false));
            if (DefinitionRepository.getSchemaMode() == SchemaMode.DEFAULT) {
                fields.add(JsonObjectUtils.createRedefinedArray(Constant.EXTENSION, "Extension"));
            } else if (DefinitionRepository.getSchemaMode() == SchemaMode.ADVANCED) {
                handleAdvancedExtension(root, fields, operation);
            }

            for (Map.Entry<String, Extension> entry : extensions.entrySet()) {
                fields.add(entry.getValue().getDefinition().convertToJson(root, entry.getKey(), entry.getValue().isRequired()));
            }

            return JsonObjectUtils.createInnerRecord(Constant.EXTENSION, formattedName, "An Extension", fields.build(), required);
        }
    }

    public static void initializeExtensions(SchemaMode schemaMode) {
        extensions = new LinkedHashMap<>();

        if (schemaMode == SchemaMode.DEFAULT) {
            extensions.put("valueBase64Binary", new Extension("base64Binary", DefinitionType.PRIMITIVE));
            extensions.put("valueBoolean", new Extension("boolean", DefinitionType.PRIMITIVE));
            extensions.put("valueCanonical", new Extension("canonical", DefinitionType.PRIMITIVE));
            extensions.put("valueCode", new Extension("code", DefinitionType.PRIMITIVE));
            extensions.put("valueTime", new Extension("time", DefinitionType.PRIMITIVE));
            extensions.put("valueId", new Extension("id", DefinitionType.PRIMITIVE));
            extensions.put("valueInteger", new Extension("integer", DefinitionType.PRIMITIVE));
            extensions.put("valueMarkdown", new Extension("markdown", DefinitionType.PRIMITIVE));
            extensions.put("valueOid", new Extension("oid", DefinitionType.PRIMITIVE));
            extensions.put("valuePositiveInt", new Extension("positiveInt", DefinitionType.PRIMITIVE));
            extensions.put("valueString", new Extension("string", DefinitionType.PRIMITIVE));
            extensions.put("valueUnsignedInt", new Extension("unsignedInt", DefinitionType.PRIMITIVE));
            extensions.put("valueUri", new Extension("uri", DefinitionType.PRIMITIVE));
            extensions.put("valueUrl", new Extension("url", DefinitionType.PRIMITIVE));
            extensions.put("valueUuid", new Extension("uuid", DefinitionType.PRIMITIVE));
            extensions.put("valueDate", new Extension("date", DefinitionType.SPECIFIC));
            extensions.put("valueDateTime", new Extension("dateTime", DefinitionType.SPECIFIC));
            extensions.put("valueInstant", new Extension("instant", DefinitionType.SPECIFIC));
            extensions.put("valueDecimal", new Extension("decimal", DefinitionType.SPECIFIC));
            extensions.put("valueIdentifier", new Extension("Identifier", DefinitionType.SPECIFIC));
            extensions.put("valueReference", new Extension("Reference", DefinitionType.SPECIFIC));
            extensions.put("valueAddress", new Extension("Address", DefinitionType.COMPLEX));
            extensions.put("valueAge", new Extension("Age", DefinitionType.COMPLEX));
            extensions.put("valueAnnotation", new Extension("Annotation", DefinitionType.COMPLEX));
            extensions.put("valueAttachment", new Extension("Attachment", DefinitionType.COMPLEX));
            extensions.put("valueContactPoint", new Extension("ContactPoint", DefinitionType.COMPLEX));
            extensions.put("valueCount", new Extension("Count", DefinitionType.COMPLEX));
            extensions.put("valueDistance", new Extension("Distance", DefinitionType.COMPLEX));
            extensions.put("valueDuration", new Extension("Duration", DefinitionType.COMPLEX));
            extensions.put("valueHumanName", new Extension("HumanName", DefinitionType.COMPLEX));
            extensions.put("valueMoney", new Extension("Money", DefinitionType.COMPLEX));
            extensions.put("valuePeriod", new Extension("Period", DefinitionType.COMPLEX));
            extensions.put("valueTriggerDefinition", new Extension("TriggerDefinition", DefinitionType.COMPLEX));
            extensions.put("valueUsageContext", new Extension("UsageContext", DefinitionType.COMPLEX));
            extensions.put("valueDosage", new Extension("Dosage", DefinitionType.COMPLEX));
            extensions.put("valueMeta", new Extension("Meta", DefinitionType.COMPLEX));
            extensions.put("valueCodeableConcept", new Extension("CodeableConcept", DefinitionType.COMPLEX));
            extensions.put("valueCoding", new Extension("Coding", DefinitionType.COMPLEX));
            extensions.put("valueQuantity", new Extension("Quantity", DefinitionType.COMPLEX));
            extensions.put("valueRange", new Extension("Range", DefinitionType.COMPLEX));
            extensions.put("valueRatio", new Extension("Ratio", DefinitionType.COMPLEX));
            extensions.put("valueSampledData", new Extension("SampledData", DefinitionType.COMPLEX));
            extensions.put("valueSignature", new Extension("Signature", DefinitionType.COMPLEX));
            extensions.put("valueTiming", new Extension("Timing", DefinitionType.COMPLEX));
            extensions.put("valueContactDetail", new Extension("ContactDetail", DefinitionType.COMPLEX));
            extensions.put("valueContributor", new Extension("Contributor", DefinitionType.COMPLEX));
            extensions.put("valueDataRequirement", new Extension("DataRequirement", DefinitionType.COMPLEX));
            extensions.put("valueExpression", new Extension("Expression", DefinitionType.COMPLEX));
            extensions.put("valueParameterDefinition", new Extension("ParameterDefinition", DefinitionType.COMPLEX));
            extensions.put("valueRelatedArtifact", new Extension("RelatedArtifact", DefinitionType.COMPLEX));
        }
    }

    public static void registerExtension(String valueType) {
        String valueName = "value" + WordUtils.capitalize(valueType);
        if (extensions.containsKey(valueName)) {
            return;
        }

        extensions.put(valueName, new Extension(valueType, DefinitionRepository.getDefinitionByIdentifier(valueType), false));
    }

    private void handleAdvancedExtension(String root, JsonArrayBuilder fields, Operation<RecursiveExtension> recursiveOperation) {
        if (recursiveOperation.isValid()) {
            RecursiveExtension recursiveExtension = recursiveOperation.getResult();
            if (recursiveExtension.getDepth() < DefinitionRepository.getRecursivityDepth()) {
                fields.add(JsonObjectUtils.createArray(Constant.EXTENSION, convertToJson(root, recursiveExtension.getAbsoluteRoot() + ".extension", false)));
            }
        } else {
            fields.add(JsonObjectUtils.createArray(Constant.EXTENSION, convertToJson(root, "Patient.extension", false)));
        }
    }

    private Operation<RecursiveExtension> checkIfRecursive(String name) {
        return (name.contains(".")) ? new Operation<>(new RecursiveExtension(name)) : new Operation<>();
    }

    private static class RecursiveExtension {

        private final String absoluteRoot;
        private final Integer depth;

        public RecursiveExtension(String absoluteRoot) {
            this.absoluteRoot = absoluteRoot;
            this.depth = Math.toIntExact(absoluteRoot.chars().filter(ch -> ch == '.').count());
        }

        public String getAbsoluteRoot() {
            return absoluteRoot;
        }

        public Integer getDepth() {
            return depth;
        }
    }

    private static class Extension {

        private String identifier;
        private IDefinition definition;
        private DefinitionType definitionType;
        private boolean required;

        public Extension(String identifier, DefinitionType definitionType) {
            this.identifier = identifier;
            this.definitionType = definitionType;
            switch (definitionType) {
                case PRIMITIVE:
                    this.definition = DefinitionRepository.getPrimitiveDefinitionByIdentifier(identifier);
                    break;
                case COMPLEX:
                    this.definition = DefinitionRepository.getComplexDefinitionByIdentifier(identifier);
                    break;
                case SPECIFIC:
                    this.definition = SpecificDefinitionFactory.getSpecificDefinition(identifier);
                    break;
            }
            // All extension has to be required = false due to the nature of an Extension.
            this.required = false;
        }

        public Extension(String identifier, IDefinition definition, boolean required) {
            setIdentifier(identifier);
            setDefinition(definition);

            // All extension has to be required = false due to the nature of an Extension.
            setRequired(required);
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public DefinitionType getDefinitionType() {
            return definitionType;
        }

        public void setDefinitionType(DefinitionType definitionType) {
            this.definitionType = definitionType;
        }

        public IDefinition getDefinition() {
            return definition;
        }

        public void setDefinition(IDefinition definition) {
            this.definition = definition;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }
    }
}
