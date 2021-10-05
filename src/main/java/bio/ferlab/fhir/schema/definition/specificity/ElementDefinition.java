package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.*;

public class ElementDefinition extends SpecificDefinition {

    @Override
    public JsonObject convertToJson(String root, String name, boolean required) {
        String formattedName = "Element";
        if (DefinitionRepository.registerInnerRecords(root, "element")) {
            return JsonObjectUtils.createRedefinedRecord(name, formattedName, Json.createObjectBuilder().build());
        } else {
            JsonArray fields = Json.createArrayBuilder()
                    .add(JsonObjectUtils.createConst("id", Constant.STRING, false))
                    .add(JsonObjectUtils.createRedefinedArray("extension", "Extension"))
                    .build();
            return JsonObjectUtils.createInnerRecord(name, formattedName, "Base definition for all elements in a resource.", fields, required);
        }
    }
}
