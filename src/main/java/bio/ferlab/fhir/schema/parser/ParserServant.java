package bio.ferlab.fhir.schema.parser;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.definition.exception.UnknownParserException;

import javax.json.JsonObject;
import java.util.List;

public class ParserServant {

    private static final List<IParser> parsers = List.of(
            new ReferenceParser(),
            new ArrayParser(),
            new EnumParser(),
            new TypeParser(),
            new ConstantParser()
    );

    private ParserServant() {
    }

    public static JsonObject parseField(String root, String identifier, Property property) {
        return parsers.stream()
                .filter(parser -> parser.canParse(property))
                .findFirst()
                .orElseThrow(() -> new UnknownParserException(identifier))
                .parseField(root, identifier, property);
    }
}
