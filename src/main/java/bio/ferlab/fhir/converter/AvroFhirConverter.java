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
import java.util.List;

import static bio.ferlab.fhir.converter.ConverterUtils.navigatePath;

public class AvroFhirConverter {

    private AvroFhirConverter() {
    }

    public static <T extends BaseResource> T readGenericRecord(GenericRecord genericRecord, Schema schema, Class<T> type) {
        ResourceContext resourceContext = new ResourceContext(TerserUtilHelper.newHelper(FhirContext.forR4(), type.getSimpleName()));
        read(resourceContext, null, schema, genericRecord);
        return resourceContext.getHelper().getResource();
    }

    protected static void read(ResourceContext context, Schema.Field field, Schema schema, Object value) {
        switch (schema.getType()) {
            case RECORD:
                readRecord(context, schema, value);
                break;
            case ARRAY:
                readArray(context, field, schema, value);
                break;
            case UNION:
                readUnion(context, field, schema, value);
                break;
            case ENUM:
                readType(context, SymbolUtils.decodeSymbol(value.toString()));
                break;
            case STRING:
            case FIXED:
            case BOOLEAN:
                readType(context, value.toString());
                break;
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BYTES:
                readNumber(context, schema, value);
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

    protected static void readRecord(ResourceContext context, Schema schema, Object value) {
        GenericRecord genericRecord = (GenericRecord) value;
        for (Schema.Field innerField : schema.getFields()) {
            if (Constant.RESOURCE_TYPE.equalsIgnoreCase(innerField.name())) {
                continue;
            }

            Object object = genericRecord.get(innerField.name());
            if (object != null) {
                context.addLastToPath(innerField.name());
                read(context, innerField, innerField.schema(), object);
            }
        }
        if (!context.getPath().isEmpty()) {
            context.getPath().removeLast();
        }
    }

    protected static void readArray(ResourceContext context, Schema.Field field, Schema schema, Object value) {
        if (!(value instanceof List)) {
            throw new AvroConversionException("Something is wrong, please verify this.");
        }

        if (((List<?>) value).isEmpty()) {
            context.getPath().removeLast();
            return;
        }

        String absolutePath = navigatePath(context.getPath());

        context.detectPathConflict(absolutePath);

        for (Object element : (List<?>) value) {
            if (element instanceof GenericRecord) {
                if (context.getArrayContext().hasNode(absolutePath)) {
                    String relativePath = navigatePath(context.getPath(), false, context.getPath().size() - 1);
                    context.getTerser().addElement(context.getArrayContext().getCurrentBase(absolutePath), relativePath);
                } else {
                    context.getTerser().addElement(context.getResource(), absolutePath);
                }
            } else {
                context.getTerser().addElement(context.getResource(), absolutePath, element.toString());
            }
            read(context, field, schema.getElementType(), element);
            context.addLastToPath(field.name());
        }

        context.getArrayContext().progressNode(absolutePath);
        context.getPath().removeLast();
    }

    protected static void readUnion(ResourceContext context, Schema.Field field, Schema schema, Object value) {
        for (Schema type : schema.getTypes()) {
            try {
                read(context, field, type, value);
            } catch (UnionTypeException ignored) {
            }
        }
    }

    protected static void readNumber(ResourceContext context, Schema schema, Object value) {
        switch (schema.getLogicalType().getName()) {
            case Constant.TIMESTAMP_MICROS:
                readType(context, DateUtils.formatTimestampMicros((Long) value));
                return;
            case Constant.DATE:
                readType(context, DateUtils.formatDate((Integer) value));
                return;
            case Constant.DECIMAL:
                readType(context, StandardCharsets.UTF_8.decode((ByteBuffer) value).toString());
                return;
            default:
                readType(context, value.toString());
        }
    }

    protected static void readType(ResourceContext context, String value) {
        String absolutePath = navigatePath(context.getPath());
        List<IBase> elements = context.getTerser().getValues(context.getHelper().getResource(), absolutePath);

        if (elements.isEmpty()) {
            context.getHelper().setField(absolutePath, value);
        } else {
            // Get the parent of the element, not the element itself.
            String relativePath = navigatePath(context.getPath(), context.getPath().size() - 1);
            elements = context.getTerser().getValues(context.getResource(), relativePath);
            for (IBase childrenElement : elements) {
                Property property = ((Base) childrenElement).getChildByName(context.getPath().getLast());

                // Ignore all the primitive values.
                if (property != null && !property.hasValues()) {
                    context.getTerser().setElement(childrenElement, context.getPath().getLast(), value);
                }
            }
        }
        context.getPath().removeLast();
    }
}