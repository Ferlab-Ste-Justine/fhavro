package bio.ferlab.fhir.schema.parser;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.utils.Constant;

import javax.json.JsonObject;

public class ReferenceParser implements IParser {

    @Override
    public boolean canParse(Property property) {
        return property.getJsonNode().has(Constant.REF);
    }

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        return DefinitionRepository.getReferenceObject(root, property.getJsonNode(), identifier, property.isRequired());
    }
}
