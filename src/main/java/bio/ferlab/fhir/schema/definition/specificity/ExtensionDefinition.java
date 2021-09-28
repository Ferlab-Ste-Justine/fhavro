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
    }};

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        String formattedName = WordUtils.capitalize(name);

        if (DefinitionRepository.registerInnerRecords(root, name)) {
            return JsonObjectUtils.createRedefinedRecord(name, formattedName, Json.createObjectBuilder().build());
        } else {
            JsonArrayBuilder fields = Json.createArrayBuilder();
            fields.add(JsonObjectUtils.createConst("url", "string", false));

            for (Map.Entry<String, String> entry : PRIMITIVES.entrySet()) {
                BaseDefinition baseDefinition = DefinitionRepository.getPrimitiveDefinitionByIdentifier(entry.getValue());
                fields.add(baseDefinition.convertToJson(root, entry.getKey(), false));
            }

            for (Map.Entry<String, BaseDefinition> entry : SPECIFICS.entrySet()) {
                fields.add(entry.getValue().convertToJson(root, entry.getKey(), false));
            }

            return JsonObjectUtils.createInnerRecord(name, formattedName, "An Extension", fields.build(), required);
        }

        // TODO support all these potential values of all these potential types.
        /*
          "url" : "<uri>", // R!  identifies the meaning of the extension
          "valueAddress" : { Address }
          "valueAge" : { Age }
          "valueAnnotation" : { Annotation }
          "valueAttachment" : { Attachment }
          "valueCodeableConcept" : { CodeableConcept }
          "valueCoding" : { Coding }
          "valueContactPoint" : { ContactPoint }
          "valueCount" : { Count }
          "valueDistance" : { Distance }
          "valueDuration" : { Duration }
          "valueHumanName" : { HumanName }
          "valueIdentifier" : { Identifier }
          "valueMoney" : { Money }
          "valuePeriod" : { Period }
          "valueQuantity" : { Quantity }
          "valueRange" : { Range }
          "valueRatio" : { Ratio }
          "valueReference" : { Reference }
          "valueSampledData" : { SampledData }
          "valueSignature" : { Signature }
          "valueTiming" : { Timing }
          "valueContactDetail" : { ContactDetail }
          "valueContributor" : { Contributor }
          "valueDataRequirement" : { DataRequirement }
          "valueExpression" : { Expression }
          "valueParameterDefinition" : { ParameterDefinition }
          "valueRelatedArtifact" : { RelatedArtifact }
          "valueTriggerDefinition" : { TriggerDefinition }
          "valueUsageContext" : { UsageContext }
          "valueDosage" : { Dosage }
          "valueMeta" : { Meta }
         */
    }
}
