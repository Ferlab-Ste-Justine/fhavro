package bio.ferlab.fhir.schema.parser;

import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.definition.exception.UnknownParserException;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.JsonObjectUtils;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayParser implements IParser {

    private static final List<IParser> innerParser = Collections.unmodifiableList(new ArrayList<IParser>() {{
        add(new ReferenceParser());
        add(new EnumParser());
    }});

    @Override
    public boolean canParse(Property property) {
        return property.getJsonNode().has(Constant.ITEMS);
    }

    @Override
    public JsonObject parseField(String root, String identifier, Property property) {
        Property items = new Property(property.getJsonNode().get(Constant.ITEMS), true);
        return JsonObjectUtils.createArray(identifier, innerParser.stream()
                .filter(parser -> parser.canParse(items))
                .findFirst()
                .orElseThrow(() -> new UnknownParserException(identifier))
                .parseField(root, identifier, items));
    }
}
