package bio.ferlab.fhir.schema.parser;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class EnumParser implements IParser {

    @Override
    public boolean canParse(Property property) {
        return property.getJsonNode().has(Constant.ENUM);
    }

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        String enumName = generateEnumName(property);
        if (DefinitionRepository.registerInnerRecords(root, enumName)) {
            return JsonObjectUtils.createRedefinedRecord(enumName, enumName, null);
        } else {
            return JsonObjectUtils.createField(identifier, JsonObjectUtils.createEnum(enumName, property.getJsonNode()), property.isRequired());
        }
    }

    // The FHIR Json Schema does not provide any names for the Enum except the name of the field. Except according to Avro specification,
    // If both entities have the same the fullname e.g: "org.foo.X", they are considered the same entity. Therefore, generates a hashcode
    // based on the symbols to avoid unwanted clash in full name.
    private String generateEnumName(Property property) {
        AtomicReference<String> content = new AtomicReference<>("");
        property.getJsonNode().get(Constant.ENUM).forEach(symbol -> content.set(content.get() + symbol.asText()));
        return UUID.nameUUIDFromBytes(content.get().getBytes(StandardCharsets.UTF_8))
                .toString()
                .replace("-", "")
                .replaceAll("\\d", "");
    }
}
