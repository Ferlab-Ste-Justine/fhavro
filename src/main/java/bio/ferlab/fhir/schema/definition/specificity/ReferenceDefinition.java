package bio.ferlab.fhir.schema.definition.specificity;

import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.repository.SchemaMode;
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
            SchemaMode schemaMode = DefinitionRepository.getSchemaMode();
            boolean isSimple = schemaMode == SchemaMode.SIMPLE || schemaMode == SchemaMode.ADVANCED;
            JsonArray fields = Json.createArrayBuilder()
                    .add(JsonObjectUtils.createConst(Constant.REFERENCE, Constant.STRING, false))
                    .add(JsonObjectUtils.createConst(Constant.TYPE, Constant.STRING, false))
                    .add(JsonObjectUtils.createConst("identifier", (isSimple) ? Constant.STRING : "Identifier", false))
                    .add(JsonObjectUtils.createConst("display", Constant.STRING, false))
                    .build();
            if (isSimple) {
                return JsonObjectUtils.createInnerRecord(name, formattedName, "A Reference", fields, required);
            } else {
                return JsonObjectUtils.createReference(name, formattedName, "A Reference", Constant.IDENTIFIER, fields, required);
            }
        }
    }
}
