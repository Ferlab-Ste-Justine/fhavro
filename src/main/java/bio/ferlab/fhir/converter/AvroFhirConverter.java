package bio.ferlab.fhir.converter;

import bio.ferlab.fhir.converter.exception.AvroConversionException;
import bio.ferlab.fhir.converter.exception.UnionTypeException;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.SymbolUtils;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.TerserUtilHelper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Property;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static bio.ferlab.fhir.converter.ConverterUtils.navigatePath;

public class AvroFhirConverter {

    private AvroFhirConverter() {
    }

    public static <T extends BaseResource> T readGenericRecord(GenericRecord genericRecord, Schema schema, Class<T> type) {
        TerserUtilHelper helper = TerserUtilHelper.newHelper(FhirContext.forR4(), type.getSimpleName());
        read(helper, null, schema, genericRecord, new ArrayDeque<>());
        return helper.getResource();
    }

    protected static void read(TerserUtilHelper helper, Schema.Field field, Schema schema, Object value, Deque<String> path) {
        switch (schema.getType()) {
            case RECORD:
                readRecord(helper, schema, value, path);
                break;
            case ARRAY:
                readArray(helper, field, schema, value, path);
                break;
            case UNION:
                readUnion(helper, field, schema, value, path);
                break;
            case ENUM:
                readType(helper, SymbolUtils.decodeSymbol(value.toString()), path);
                break;
            case STRING:
            case FIXED:
            case BOOLEAN:
                readType(helper, value.toString(), path);
                break;
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BYTES:
                readNumber(helper, schema, value, path);
                break;
            case NULL:
                if (value == null) {
                    return;
                }
                throw new UnionTypeException();
            default:
                throw new AvroConversionException(String.format("The following type is unknown: %s", schema.getType()));
        }
    }

    protected static void readRecord(TerserUtilHelper helper, Schema schema, Object value, Deque<String> path) {
        GenericRecord genericRecord = (GenericRecord) value;
        for (Schema.Field innerField : schema.getFields()) {
            if (Constant.RESOURCE_TYPE.equalsIgnoreCase(innerField.name())) {
                continue;
            }

            Object object = genericRecord.get(innerField.name());
            if (object != null) {
                addLast(path, innerField.name());
                read(helper, innerField, innerField.schema(), object, path);
            }
        }
        if (!path.isEmpty()) {
            path.removeLast();
        }
    }

    protected static void readArray(TerserUtilHelper helper, Schema.Field field, Schema schema, Object value, Deque<String> path) {
        if (!(value instanceof List)) {
            throw new AvroConversionException("Something is wrong, please verify this.");
        }

        for (Object element : (List) value) {
            if (element instanceof GenericRecord) {
                helper.getTerser().addElement(helper.getResource(), navigatePath(path));
            } else {
                helper.getTerser().addElement(helper.getResource(), navigatePath(path), element.toString());
            }
            read(helper, field, schema.getElementType(), element, path);
            addLast(path, field.name());
        }

        path.removeLast();
    }

    protected static void readUnion(TerserUtilHelper helper, Schema.Field field, Schema schema, Object value, Deque<String> path) {
        for (Schema type : schema.getTypes()) {
            try {
                read(helper, field, type, value, path);
            } catch (UnionTypeException ignored) {
            }
        }
    }

    protected static void readNumber(TerserUtilHelper helper, Schema schema, Object value, Deque<String> path) {
        switch (schema.getLogicalType().getName()) {
            case Constant.TIMESTAMP_MICROS:
                readType(helper, DateUtils.formatTimestampMicros((Long) value), path);
                return;
            case Constant.DATE:
                readType(helper, DateUtils.formatDate((Integer) value), path);
                return;
            case Constant.DECIMAL:
                readType(helper, StandardCharsets.UTF_8.decode((ByteBuffer) value).toString(), path);
                return;
            default:
                readType(helper, value.toString(), path);
        }
    }

    protected static void readType(TerserUtilHelper helper, String value, Deque<String> path) {
        String absolutePath = navigatePath(path);
        List<IBase> elements = helper.getTerser().getValues(helper.getResource(), absolutePath);

        if (elements.isEmpty()) {
            helper.setField(absolutePath, value);
        } else {
            // Get the parent of the element, not the element itself.
            String relativePath = navigatePath(path, path.size() - 1);
            elements = helper.getTerser().getValues(helper.getResource(), relativePath);
            for (IBase childrenElement : elements) {
                Property property = ((Base) childrenElement).getChildByName(path.getLast());

                // Ignore all the primitive values.
                if (property != null && !property.hasValues()) {
                    helper.getTerser().setElement(childrenElement, path.getLast(), value);
                }
            }
        }
        path.removeLast();
    }

    protected static void addLast(Deque<String> path, String value) {
        // Format value[x] in camelCase
        if (value.contains(Constant.VALUE) && value.length() >= 6) {
            value = value.substring(0, 5) + String.valueOf(value.charAt(5)).toUpperCase() + value.substring(6);
        }
        path.addLast(value);
    }
}