package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.definition.BaseDefinition;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;
import org.apache.commons.text.WordUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;

public class ExtensionDefinition extends SpecificDefinition {

    private static final Map<String, String> PRIMITIVES = new HashMap<>() {{
        put("valueBase64Binary", "base64Binary");
        put("valueBoolean", "boolean");
        put("valueCanonical", "canonical");
        put("valueCode", "code");
        put("valueTime", "time");
        put("valueId", "id");
        put("valueInstant", "instant");
        put("valueInteger", "integer");
        put("valueMarkdown", "markdown");
        put("valueOid", "oid");
        put("valuePositiveInt", "positiveInt");
        put("valueString", "string");
        put("valueUnsignedInt", "unsignedInt");
        put("valueUri", "uri");
        put("valueUrl", "url");
        put("valueUuid", "uuid");
    }};

    private static final Map<String, BaseDefinition> SPECIFICS = new HashMap<>() {{
        put("valueDate", new DateDefinition());
        put("valueDateTime", new DateTimeDefinition());
        put("valueDecimal", new DecimalDefinition());
        put("valueReference", new ReferenceDefinition());
    }};

    private static final Map<String, String> COMPLEXES = new HashMap<>() {{
        put("valueAddress", "Address");
        put("valueAge", "Age");
        put("valueAnnotation", "Annotation");
        put("valueAttachment", "Attachment");
        put("valueContactPoint", "ContactPoint");
        put("valueCount", "Count");
        put("valueDistance", "Distance");
        put("valueDuration", "Duration");
        put("valueHumanName", "HumanName");
        put("valueMoney", "Money");
        put("valuePeriod", "Period");
        put("valueTriggerDefinition", "TriggerDefinition");
        put("valueUsageContext", "UsageContext");
        put("valueDosage", "Dosage");
        put("valueMeta", "Meta");
        put("valueCodeableConcept", "CodeableConcept");
        put("valueCoding", "Coding");
        put("valueIdentifier", "Identifier");
        put("valueQuantity", "Quantity");
        put("valueRange", "Range");
        put("valueRatio", "Ratio");
        put("valueSampledData", "SampledData");
        put("valueSignature", "Signature");
        put("valueTiming", "Timing");
        put("valueContactDetail", "ContactDetail");
        put("valueContributor", "Contributor");
        put("valueDataRequirement", "DataRequirement");
        put("valueExpression", "Expression");
        put("valueParameterDefinition", "ParameterDefinition");
        put("valueRelatedArtifact", "RelatedArtifact");
    }};

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        String formattedName = WordUtils.capitalize(name);
        if (DefinitionRepository.registerInnerRecords(root, name)) {
            return JsonObjectUtils.createRedefinedRecord(name, formattedName, Json.createObjectBuilder().build());
        } else {
            JsonArrayBuilder fields = Json.createArrayBuilder();
            fields.add(JsonObjectUtils.createConst("url", "string", false));

            for (Map.Entry<String, String> entry : COMPLEXES.entrySet()) {
                BaseDefinition baseDefinition = DefinitionRepository.getComplexDefinitionByIdentifier(entry.getValue());
                fields.add(baseDefinition.convertToJson(root, entry.getKey(), false));
            }

            for (Map.Entry<String, BaseDefinition> entry : SPECIFICS.entrySet()) {
                fields.add(entry.getValue().convertToJson(root, entry.getKey(), false));
            }

            for (Map.Entry<String, String> entry : PRIMITIVES.entrySet()) {
                BaseDefinition baseDefinition = DefinitionRepository.getPrimitiveDefinitionByIdentifier(entry.getValue());
                fields.add(baseDefinition.convertToJson(root, entry.getKey(), false));
            }

            return JsonObjectUtils.createInnerRecord(name, formattedName, "An Extension", fields.build(), required);
        }
    }
}
