package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;
import org.apache.commons.text.WordUtils;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class ReferenceDefinition extends SpecificDefinition {

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        String formattedName = WordUtils.capitalize(Constant.REFERENCE);
        if (DefinitionRepository.registerInnerRecords(root, Constant.REFERENCE)) {
            return JsonObjectUtils.createRedefinedRecord(name, formattedName, Json.createObjectBuilder().build());
        } else {
            JsonArray fields = Json.createArrayBuilder()
                    .add(JsonObjectUtils.createConst(Constant.REFERENCE, Constant.STRING, false))
                    .add(JsonObjectUtils.createConst(Constant.TYPE, Constant.STRING, false))
                    .add(JsonObjectUtils.createConst(Constant.IDENTIFIER, WordUtils.capitalize(Constant.IDENTIFIER), false))
                    .add(JsonObjectUtils.createConst("display", Constant.STRING, false))
                    .build();
            return JsonObjectUtils.createReference(name, formattedName, "A Reference", Constant.IDENTIFIER, fields, required);
        }
    }
}
