package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;
import org.apache.commons.text.WordUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

public class IdentifierDefinition extends SpecificDefinition {

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        String capitalizedName = WordUtils.capitalize(Constant.IDENTIFIER);
        if (DefinitionRepository.registerInnerRecords(root, Constant.IDENTIFIER)) {
            return JsonObjectUtils.createRedefinedRecord(name, capitalizedName, Json.createObjectBuilder().build());
        } else {
            JsonArrayBuilder fields = Json.createArrayBuilder()
                    .add(JsonObjectUtils.createConst("use", Constant.STRING, false))
                    .add(JsonObjectUtils.createConst("value", Constant.STRING, false))
                    .add(JsonObjectUtils.createConst("system", Constant.STRING, false))
                    .add(DefinitionRepository.getComplexDefinitionByIdentifier("CodeableConcept").convertToJson(root, Constant.TYPE, false))
                    .add(DefinitionRepository.getComplexDefinitionByIdentifier("Period").convertToJson(root, "period", false))
                    .add(DefinitionRepository.getSpecificDefinitionByIdentifier("Reference").convertToJson(root, "assigner", false));

            JsonObject identifier = Json.createObjectBuilder()
                    .add(Constant.NAME, capitalizedName)
                    .add(Constant.TYPE, Constant.RECORD)
                    .add(Constant.DOC, "An identifier - identifies some entity uniquely and unambiguously. Typically this is used for business identifiers.")
                    .add(Constant.FIELDS, fields)
                    .add(Constant.LOGICAL_TYPE, Constant.REFERENCEABLE)
                    .add(Constant.ID_FIELD_NAME, Constant.VALUE)
                    .add(Constant.DEFAULT, JsonValue.EMPTY_JSON_OBJECT)
                    .build();

            return Json.createObjectBuilder()
                    .add(Constant.NAME, name)
                    .add(Constant.TYPE, identifier)
                    .add(Constant.DEFAULT, JsonValue.EMPTY_JSON_OBJECT)
                    .build();
        }
    }
}
