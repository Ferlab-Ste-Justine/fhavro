package bio.ferlab.fhir.schema.parser;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.definition.exception.UnknownParserException;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ParserServant {

    public static final List<IParser> parsers = Collections.unmodifiableList(new ArrayList<IParser>() {{
        add(new ReferenceParser());
        add(new ArrayParser());
        add(new EnumParser());
        add(new TypeParser());
        add(new ConstantParser());
    }});

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
