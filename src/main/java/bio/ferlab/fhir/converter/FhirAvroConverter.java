package bio.ferlab.fhir.converter;

import bio.ferlab.fhir.converter.converters.DateConverter;
import bio.ferlab.fhir.converter.converters.DateTimeConverter;
import bio.ferlab.fhir.converter.converters.IConverter;
import bio.ferlab.fhir.converter.converters.InstantConverter;
import bio.ferlab.fhir.converter.exception.UnionTypeException;
import bio.ferlab.fhir.schema.utils.Constant;
import org.apache.avro.AvroTypeException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.commons.text.WordUtils;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Property;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

public class FhirAvroConverter {

    private static final Map<String, IConverter<String>> PRIMITIVE_CONVERTERS = new HashMap<>() {{
        put("date", new DateConverter());
        put("dateTime", new DateTimeConverter());
        put("instant", new InstantConverter());
    }};

    private FhirAvroConverter() {
    }

    public static GenericData.Record readResource(BaseResource baseResource, Schema schema) {
        GenericData.Record genericRecord = (GenericData.Record) FhirAvroConverter.read(schema, List.of(baseResource));
        genericRecord.put(Constant.RESOURCE_TYPE, schema.getName());
        return genericRecord;
    }

    protected static Object read(Schema schema, List<Base> bases) {
        switch (schema.getType()) {
            case RECORD:
                return readRecord(schema, bases);
            case ARRAY:
                return readArray(schema, bases);
            case UNION:
                return readUnion(schema, bases);
            case INT:
                return readType(bases, Integer::valueOf);
            case LONG:
                return readType(bases, Long::valueOf);
            case FLOAT:
                return readType(bases, Float::valueOf);
            case DOUBLE:
                return readType(bases, Double::valueOf);
            case BOOLEAN:
                return readType(bases, Boolean::parseBoolean);
            case ENUM:
            case STRING:
                return readType(bases, string -> string);
            case BYTES:
                return readType(bases, FhirAvroConverter::bytesForString);
            case NULL:
                break;
            default:
                throw new AvroTypeException("Unsupported type: " + schema.getType());
        }
        return null;
    }

    protected static Object readRecord(Schema schema, List<Base> bases) {
        GenericRecordBuilder recordBuilder = new GenericRecordBuilder(schema);

        for (Base base : bases) {
            for (Schema.Field field : schema.getFields()) {
                if (!readSpecificField(recordBuilder, base, field)) {
                    getProperty(base, field).ifPresent(property -> recordBuilder.set(field.name(), read(field.schema(), property.getValues())));
                }
            }
        }

        try {
            recordBuilder.set(Constant.RESOURCE_TYPE, schema.getName());
        } catch (Exception ignored) {
        }

        return recordBuilder.build();
    }

    protected static List<Object> readArray(Schema schema, List<Base> bases) {
        List<Object> objects = new ArrayList<>();

        for (Base base : bases) {
            objects.add(read(schema.getElementType(), Collections.singletonList(base)));
        }

        return objects;
    }

    protected static Object readUnion(Schema schema, List<Base> bases) {
        for (Base base : bases) {
            for (Schema type : schema.getTypes()) {
                try {
                    Object unionValue = FhirAvroConverter.read(type, Collections.singletonList(base));
                    if (unionValue != null) {
                        return unionValue;
                    }
                } catch (UnionTypeException ignored) {
                }
            }
        }
        return null;
    }

    protected static <T> Object readType(List<Base> bases, Function<String, T> function) {
        Base base = ConverterUtils.getBase(bases);
        String value = formatPrimitiveValue(base, base.primitiveValue());
        try {
            return function.apply(value);
        } catch (Exception ex) {
            throw new UnionTypeException();
        }
    }

    protected static String formatPrimitiveValue(Base base, String value) {
        if (PRIMITIVE_CONVERTERS.containsKey(base.fhirType())) {
            return PRIMITIVE_CONVERTERS.get(base.fhirType()).convert(value);
        }
        return value;
    }

    private static ByteBuffer bytesForString(String string) {
        return ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
    }

    private static Optional<Property> getProperty(Base base, Schema.Field field) {
        Property property;

        // Support value[x] notation.
        if (Pattern.compile("value[a-zA-Z].*").matcher(field.name()).matches()) {
            property = base.getNamedProperty(Constant.VALUE);
            if (property != null && property.hasValues()) {
                // Try to find the valid corresponding value[x] by comparing the FhirType and the field name.
                String fhirType = property.getValues().get(0).fhirType().toLowerCase();
                String fieldName = field.name().replace("value", "").toLowerCase();
                if (fieldName.equals(fhirType)) {
                    return Optional.of(property);
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        }

        property = base.getNamedProperty(WordUtils.uncapitalize(field.name()));
        if (property != null) {
            return Optional.of(property);
        }

        for (Property children : base.children()) {
            if (field.name().equalsIgnoreCase(children.getName())) {
                return Optional.of(children);
            }
        }
        return Optional.empty();
    }

    // XhtmlNode does not inherit from Base therefore cannot be made into a Property.
    private static boolean readSpecificField(GenericRecordBuilder recordBuilder, Base base, Schema.Field field) {
        if (field.name().equals("div")) {
            recordBuilder.set(field.name(), base.castToNarrative(base).getDiv().getValue());
            return true;
        }
        return false;
    }
}