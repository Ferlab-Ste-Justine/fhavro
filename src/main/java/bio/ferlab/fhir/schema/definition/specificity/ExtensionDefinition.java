package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.definition.IDefinition;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
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
        String formattedName = WordUtils.capitalize(name);
        if (DefinitionRepository.registerInnerRecords(root, name)) {
            return JsonObjectUtils.createRedefinedRecord(name, formattedName, Json.createObjectBuilder().build());
        } else {
            JsonArrayBuilder fields = Json.createArrayBuilder();
            fields.add(JsonObjectUtils.createConst("url", "string", false));
            fields.add(JsonObjectUtils.createRedefinedArray("extension", "Extension"));

            for (Map.Entry<String, Extension> entry : extensions.entrySet()) {
                fields.add(entry.getValue().getDefinition().convertToJson(root, entry.getKey(), entry.getValue().isRequired()));
            }

            return JsonObjectUtils.createInnerRecord(name, formattedName, "An Extension", fields.build(), required);
        }
    }

    public static void initializeExtensions() {
        if (extensions != null) {
            return;
        }

        extensions = new LinkedHashMap<>() {{
            put("valueBase64Binary", new Extension("base64Binary", DefinitionType.PRIMITIVE));
            put("valueBoolean", new Extension("boolean", DefinitionType.PRIMITIVE));
            put("valueCanonical", new Extension("canonical", DefinitionType.PRIMITIVE));
            put("valueCode", new Extension("code", DefinitionType.PRIMITIVE));
            put("valueTime", new Extension("time", DefinitionType.PRIMITIVE));
            put("valueId", new Extension("id", DefinitionType.PRIMITIVE));
            put("valueInteger", new Extension("integer", DefinitionType.PRIMITIVE));
            put("valueMarkdown", new Extension("markdown", DefinitionType.PRIMITIVE));
            put("valueOid", new Extension("oid", DefinitionType.PRIMITIVE));
            put("valuePositiveInt", new Extension("positiveInt", DefinitionType.PRIMITIVE));
            put("valueString", new Extension("string", DefinitionType.PRIMITIVE));
            put("valueUnsignedInt", new Extension("unsignedInt", DefinitionType.PRIMITIVE));
            put("valueUri", new Extension("uri", DefinitionType.PRIMITIVE));
            put("valueUrl", new Extension("url", DefinitionType.PRIMITIVE));
            put("valueUuid", new Extension("uuid", DefinitionType.PRIMITIVE));
            put("valueDate", new Extension("date", DefinitionType.SPECIFIC));
            put("valueDateTime", new Extension("dateTime", DefinitionType.SPECIFIC));
            put("valueInstant", new Extension("instant", DefinitionType.SPECIFIC));
            put("valueDecimal", new Extension("decimal", DefinitionType.SPECIFIC));
            put("valueIdentifier", new Extension("Identifier", DefinitionType.SPECIFIC));
            put("valueReference", new Extension("Reference", DefinitionType.SPECIFIC));
            put("valueAddress", new Extension("Address", DefinitionType.COMPLEX));
            put("valueAge", new Extension("Age", DefinitionType.COMPLEX));
            put("valueAnnotation", new Extension("Annotation", DefinitionType.COMPLEX));
            put("valueAttachment", new Extension("Attachment", DefinitionType.COMPLEX));
            put("valueContactPoint", new Extension("ContactPoint", DefinitionType.COMPLEX));
            put("valueCount", new Extension("Count", DefinitionType.COMPLEX));
            put("valueDistance", new Extension("Distance", DefinitionType.COMPLEX));
            put("valueDuration", new Extension("Duration", DefinitionType.COMPLEX));
            put("valueHumanName", new Extension("HumanName", DefinitionType.COMPLEX));
            put("valueMoney", new Extension("Money", DefinitionType.COMPLEX));
            put("valuePeriod", new Extension("Period", DefinitionType.COMPLEX));
            put("valueTriggerDefinition", new Extension("TriggerDefinition", DefinitionType.COMPLEX));
            put("valueUsageContext", new Extension("UsageContext", DefinitionType.COMPLEX));
            put("valueDosage", new Extension("Dosage", DefinitionType.COMPLEX));
            put("valueMeta", new Extension("Meta", DefinitionType.COMPLEX));
            put("valueCodeableConcept", new Extension("CodeableConcept", DefinitionType.COMPLEX));
            put("valueCoding", new Extension("Coding", DefinitionType.COMPLEX));
            put("valueQuantity", new Extension("Quantity", DefinitionType.COMPLEX));
            put("valueRange", new Extension("Range", DefinitionType.COMPLEX));
            put("valueRatio", new Extension("Ratio", DefinitionType.COMPLEX));
            put("valueSampledData", new Extension("SampledData", DefinitionType.COMPLEX));
            put("valueSignature", new Extension("Signature", DefinitionType.COMPLEX));
            put("valueTiming", new Extension("Timing", DefinitionType.COMPLEX));
            put("valueContactDetail", new Extension("ContactDetail", DefinitionType.COMPLEX));
            put("valueContributor", new Extension("Contributor", DefinitionType.COMPLEX));
            put("valueDataRequirement", new Extension("DataRequirement", DefinitionType.COMPLEX));
            put("valueExpression", new Extension("Expression", DefinitionType.COMPLEX));
            put("valueParameterDefinition", new Extension("ParameterDefinition", DefinitionType.COMPLEX));
            put("valueRelatedArtifact", new Extension("RelatedArtifact", DefinitionType.COMPLEX));
        }};
    }

    public static void registerExtension(String valueName, String valueType, boolean required) {
        extensions.values().stream()
                .filter(extension -> extension.getIdentifier().equals(valueType))
                .findFirst()
                .ifPresent(x -> extensions.put(valueName, new Extension(valueType, x.getDefinitionType(), required)));
    }

    private static class Extension {

        private String identifier;
        private IDefinition definition;
        private DefinitionType definitionType;
        private boolean required;

        public Extension(String identifier, DefinitionType definitionType) {
            this(identifier, definitionType, false);
        }

        public Extension(String identifier, DefinitionType definitionType, boolean required) {
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
