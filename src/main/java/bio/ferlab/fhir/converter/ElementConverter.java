package bio.ferlab.fhir.converter;

import bio.ferlab.fhir.converter.exception.LogicException;
import bio.ferlab.fhir.converter.exception.UnionTypeException;
import bio.ferlab.fhir.schema.utils.Constant;
import ca.uhn.fhir.context.FhirContext;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.hl7.fhir.r4.model.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ElementConverter {

    private static final FhirContext fhirContext;

    ElementConverter() {
    }

    static {
        fhirContext = FhirContext.forR4();
    }

    public static Extension readExtension(GenericRecord genericRecord) {
        return (Extension) readRecord(genericRecord.getSchema(), genericRecord);
    }

    private static Object read(Schema.Field field, Schema schema, Object value) {
        switch (schema.getType()) {
            case RECORD:
                return readRecord(schema, (GenericRecord) value);
            case ARRAY:
                return readArray(field, schema, (GenericData.Array) value);
            case MAP:
            case UNION:
                return readUnion(field, schema, value);
            case ENUM:
            case FIXED:
            case STRING:
                return readType(field, value);
            case BYTES:
                return new StringType(StandardCharsets.UTF_8.decode((ByteBuffer) value).toString());
            case INT:
            case LONG:
            case DOUBLE:
            case FLOAT:
                return new IntegerType(value.toString());
            case BOOLEAN:
                return new BooleanType(value.toString());
            case NULL:
                return null;
        }
        return null;
    }

    private static Type readRecord(Schema schema, GenericRecord genericRecord) {
        Base base = newBaseInstance(schema.getName());

        boolean isNotEmpty = false;
        for (Schema.Field field : schema.getFields()) {
            if (field.name().startsWith("_")) {
                continue;
            }

            if (genericRecord.hasField(field.name()) && genericRecord.get(field.name()) != null) {
                Object object = read(field, field.schema(), genericRecord.get(field.name()));
                if (object != null) {
                    isNotEmpty |= parseProperty(field, base, object);
                }
            }
        }
        return (isNotEmpty) ? (Type) base : null;
    }

    private static List<Object> readArray(Schema.Field field, Schema schema, List<Object> array) {
        if (array.isEmpty()) {
            // This is intentional to return null and not an empty collection.
            return null;
        }

        return array.stream()
                .map(object -> read(field, schema.getElementType(), object))
                .collect(Collectors.toList());
    }

    private static Type readUnion(Schema.Field field, Schema schema, Object value) {
        for (Schema type : schema.getTypes()) {
            try {
                Object unionValue = read(field, type, value);
                if (unionValue != null) {
                    return (Type) unionValue;
                }
            } catch (UnionTypeException ignored) {
            }
        }
        return null;
    }

    private static Type readType(Schema.Field field, Object value) {
        if (field.name().startsWith(Constant.VALUE) && !field.name().equals(Constant.VALUE)) {
            String elementType = field.name().replace(Constant.VALUE, "");
            return ((StringType) newBaseInstance(elementType)).setValue(value.toString());
        } else {
            return new StringType(value.toString());
        }
    }

    private static boolean parseProperty(Schema.Field field, Base base, Object object) {
        if (object instanceof Type) {
            return setProperty(field, base, (Type) object);
        } else if (object instanceof List) {
            return setProperties(field, base, ((List<Base>) object));
        }
        return false;
    }

    private static boolean setProperty(Schema.Field field, Base base, Type type) {
        if (type.isPrimitive()) {
            return setFieldProperty(field, base, type);
        } else {
            return type.children().stream()
                    .filter(Property::hasValues)
                    .anyMatch(x -> setFieldProperty(field, base, type));
        }
    }

    private static boolean setProperties(Schema.Field field, Base base, List<Base> bases) {
        bases.forEach(innerBase -> base.setProperty(field.name(), innerBase));
        return true;
    }

    private static boolean setFieldProperty(Schema.Field field, Base base, Type type) {
        if (field.name().startsWith(Constant.VALUE) && !field.name().equals(Constant.VALUE)) {
            base.setProperty(Constant.VALUE + "[x]", type);
        } else {
            base.setProperty(field.name(), type);
        }
        return true;
    }

    private static Base newBaseInstance(String elementType) {
        return (Base) Optional.ofNullable(fhirContext.getElementDefinition(elementType))
                .orElseThrow(LogicException::new)
                .newInstance();
    }
}
