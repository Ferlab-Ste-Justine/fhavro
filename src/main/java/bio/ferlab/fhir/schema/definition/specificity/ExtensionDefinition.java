package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.Json;
import javax.json.JsonObject;

public class ExtensionDefinition extends SpecificDefinition {

    // TODO Find a way to support variable value type.
    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        if (getJsonObject() != null) {
            return getJsonObject();
        }

        // TODO add the required if any.
        setJsonObject(Json.createObjectBuilder()
                .add(Constant.TYPE, Constant.RECORD)
                .add(Constant.NAME, "extension")
                .add(Constant.NAMESPACE, Constant.NAMESPACE_VALUE)
                .add(Constant.FIELDS, Json.createArrayBuilder()
                        .add(JsonObjectUtils.createConst("url", Constant.STRING, false))
                        .add(JsonObjectUtils.createConst("value[x]", Constant.STRING, false))
                        .build())
                .add(Constant.DEFAULT, JsonObject.NULL)
                .build());
        return getJsonObject();

        // TODO support all these potential values of all these potential types.
        /*
          "url" : "<uri>", // R!  identifies the meaning of the extension
          // value[x]: Value of extension. One of these 50:
          "valueBase64Binary" : "<base64Binary>"
          "valueBoolean" : <boolean>
          "valueCanonical" : "<canonical>"
          "valueCode" : "<code>"
          "valueDate" : "<date>"
          "valueDateTime" : "<dateTime>"
          "valueDecimal" : <decimal>
          "valueId" : "<id>"
          "valueInstant" : "<instant>"
          "valueInteger" : <integer>
          "valueMarkdown" : "<markdown>"
          "valueOid" : "<oid>"
          "valuePositiveInt" : "<positiveInt>"
          "valueString" : "<string>"
          "valueTime" : "<time>"
          "valueUnsignedInt" : "<unsignedInt>"
          "valueUri" : "<uri>"
          "valueUrl" : "<url>"
          "valueUuid" : "<uuid>"
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
