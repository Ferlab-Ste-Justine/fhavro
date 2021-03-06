package bio.ferlab.fhir.schema.parser;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.parser.regex.DateParser;
import bio.ferlab.fhir.schema.parser.regex.DateTimeParser;
import bio.ferlab.fhir.schema.parser.regex.DecimalParser;
import bio.ferlab.fhir.schema.parser.regex.StringParser;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.SchemaUtils;

import javax.json.JsonObject;
import java.util.List;
import java.util.Optional;

public class TypeParser implements IParser {

    private static final List<IParser> INNER_PARSER = List.of(new StringParser(), new DateTimeParser(), new DateParser(), new DecimalParser());

    @Override
    public boolean canParse(Property property) {
        return property.getJsonNode().has(Constant.TYPE);
    }

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        Optional<IParser> parser = INNER_PARSER.stream()
                .filter(x -> x.canParse(property))
                .findFirst();
        if (parser.isPresent()) {
            return parser.get().parseField(root, identifier, property);
        }

        String reference = SchemaUtils.parsePrimitiveType(property.getJsonNode().get(Constant.TYPE).toString());
        return DefinitionRepository.getPrimitiveDefinitionByIdentifier(reference).convertToJson(root, identifier, property.isRequired());
    }
}
