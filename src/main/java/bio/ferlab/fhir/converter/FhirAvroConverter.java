package bio.ferlab.fhir.converter;

import bio.ferlab.fhir.converter.converters.DateConverter;
import bio.ferlab.fhir.converter.converters.DateTimeConverter;
import bio.ferlab.fhir.converter.converters.IConverter;
import bio.ferlab.fhir.converter.exception.AvroConversionException;
import bio.ferlab.fhir.converter.exception.UnionTypeException;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.SymbolUtils;
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

    private static final List<IConverter<String>> primitiveConverters = new ArrayList<IConverter<String>>() {{
        add(new DateConverter());
        add(new DateTimeConverter());
    }};

    private FhirAvroConverter() {
    }

    public static GenericData.Record readResource(BaseResource baseResource, Schema schema) {
        Object object = FhirAvroConverter.read(schema, Arrays.asList(baseResource));
        GenericData.Record genericRecord = (GenericData.Record) object;
        genericRecord.put(Constant.RESOURCE_TYPE, schema.getName());
        return genericRecord;
    }

    protected static Object read(Schema schema, List<Base> bases) {
        switch (schema.getType()) {
            case RECORD:
                return readRecord(schema, bases);
            case ENUM:
                return readEnum(schema, bases);
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
                getProperty(base, field).ifPresent(property -> recordBuilder.set(field.name(), read(field.schema(), property.getValues())));
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

    protected static Object readEnum(Schema schema, List<Base> bases) {
        Base base = getSingle(bases);
        List<String> symbols = schema.getEnumSymbols();
        String encodedSymbol = SymbolUtils.encodeSymbol(base.primitiveValue());
        if (symbols.contains(encodedSymbol)) {
            return new GenericData.EnumSymbol(schema, encodedSymbol);
        } else {
            throw new AvroConversionException(String.format("value: %s was not found within Symbols: %s", base.primitiveValue(), symbols));
        }
    }

    protected static <T> Object readType(List<Base> bases, Function<String, T> function) {
        Base base = getSingle(bases);
        String value = formatPrimitiveValue(base.primitiveValue());
        try {
            return function.apply(value);
        } catch (Exception ex) {
            throw new UnionTypeException();
        }
    }

    protected static String formatPrimitiveValue(String value) {
        for (IConverter<String> converter : primitiveConverters) {
            if (Pattern.compile(converter.getPattern()).matcher(value).matches()) {
                return converter.convert(value);
            }
        }
        return value;
    }

    private static ByteBuffer bytesForString(String string) {
        return ByteBuffer.wrap(string.getBytes(StandardCharsets.UTF_8));
    }

    private static Base getSingle(List<Base> bases) {
        return Optional.ofNullable(bases.get(0))
                .orElseThrow(() -> new RuntimeException("Please verify this, this isn't suppose to occur."));
    }

    private static Optional<Property> getProperty(Base base, Schema.Field field) {
        Property property = base.getNamedProperty(WordUtils.uncapitalize(field.name()));
        if (property != null) {
            return Optional.of(property);
        }

        // Support value[x] notation.
        if (field.name().contains(Constant.VALUE)) {
            property = base.getNamedProperty(Constant.VALUE);
            if (property != null) {
                return Optional.of(property);
            }
        }

        for (Property children : base.children()) {
            if (field.name().equalsIgnoreCase(children.getName())) {
                return Optional.of(children);
            }
        }
        return Optional.empty();
    }
}