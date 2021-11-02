package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;
import org.apache.commons.text.WordUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class IdentifierDefinition extends SpecificDefinition {

    private static final String DOC = "An identifier - identifies some entity uniquely and unambiguously. Typically this is used for business identifiers.";

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        String capitalizedName = WordUtils.capitalize(Constant.IDENTIFIER);
        if (DefinitionRepository.registerInnerRecords(root, Constant.IDENTIFIER)) {
            return JsonObjectUtils.createRedefinedRecord(name, capitalizedName, Json.createObjectBuilder().build());
        } else {
            SchemaMode schemaMode = DefinitionRepository.getSchemaMode();
            JsonArray fields = Json.createArrayBuilder()
                    .add(JsonObjectUtils.createConst("use", Constant.STRING, false))
                    .add(JsonObjectUtils.createConst("value", Constant.STRING, false))
                    .add(JsonObjectUtils.createConst("system", Constant.STRING, false))
                    .add(DefinitionRepository.getComplexDefinitionByIdentifier("CodeableConcept").convertToJson(root, Constant.TYPE, false))
                    .add(DefinitionRepository.getComplexDefinitionByIdentifier("Period").convertToJson(root, "period", false))
                    .add(DefinitionRepository.getSpecificDefinitionByIdentifier("Reference").convertToJson(root, "assigner", false))
                    .build();
            if (schemaMode == SchemaMode.SIMPLE || schemaMode == SchemaMode.ADVANCED) {
                return JsonObjectUtils.createInnerRecord(name, WordUtils.capitalize(Constant.IDENTIFIER), DOC, fields, false);
            } else {
                return JsonObjectUtils.createReferenceable(name, WordUtils.capitalize(Constant.IDENTIFIER), DOC, Constant.VALUE, fields, false);
            }
        }
    }
}
