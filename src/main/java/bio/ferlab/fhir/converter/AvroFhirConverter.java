package bio.ferlab.fhir.converter;

import bio.ferlab.fhir.converter.exception.AvroConversionException;
import bio.ferlab.fhir.converter.exception.UnionTypeException;
import bio.ferlab.fhir.schema.utils.Constant;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.util.TerserUtilHelper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Property;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
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

        // Build the Array elements.
        for (Object element : (List<?>) value) {
            Operation<String> operation = findRegisteredNode(context);
            if (operation.isValid()) {
                Base base = (Base) context.getArrayContext().getCurrentBase(operation.getResult());
                String relativePath = navigatePath(context.getPath(), true, context.getPath().size(), 1);
                if (element instanceof GenericRecord) {
                    context.getTerser().addElement(base, relativePath);
                } else {
                    context.getTerser().addElement(base, relativePath, element.toString());
                }
            } else {
                context.getTerser().addElement(context.getResource(), absolutePath);
            }
        }

        context.detectPathConflict(context.getPath());

        // Populate the Array.
        for (Object element : (List<?>) value) {
            read(context, field, schema.getElementType(), element);

            context.getArrayContext().progressNode(absolutePath);
            context.addLastToPath(field.name());
        }

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
        if (schema.getLogicalType() == null) {
            readType(context, value.toString());
            return;
        }

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
        if (readSpecificField(context, value)) {
            return;
        }

        String root = navigatePath(context.getPath(), context.getPath().size() - 1);
        // The field is not found directly from the root, we need to dig deeper.
        if (!context.getArrayContext().hasNode(root)) {
            // There can be multiple occurrences, get all of them.
            Operation<List<String>> operation = findAllRegisteredNodes(context);
            if (operation.isValid() && !operation.getResult().isEmpty()) {
                int count = 1;
                for (String result : operation.getResult()) {
                    Base parentBase = (Base) context.getArrayContext().getCurrentBase(result);
                    if (parentBase.isPrimitive()) {
                        continue;
                    }

                    String relativePath = navigatePath(context.getPath(), true, context.getPath().size(), count);
                    List<IBase> elements = context.getTerser().getValues(parentBase, relativePath);
                    if (elements.isEmpty()) {
                        context.getTerser().setElement(
                                parentBase,
                                relativePath,
                                value);
                        break;
                    } else {
                        // Unfortunately, not yet found, keep digging.
                        count++;
                    }
                }
            } else {
                context.getHelper().setField(navigatePath(context.getPath()), value);
            }
        } else {
            String absolutePath = navigatePath(context.getPath());
            if (!context.getArrayContext().hasNode(absolutePath)) {
                context.getTerser().setElement(
                        context.getArrayContext().getCurrentBase(root),
                        context.getPath().getLast(),
                        value);
            }
        }
        context.getPath().removeLast();
    }

    // <div> field is not a primitive type, but a xhtmlNode (which extends PrimitiveType but does not extend Element)
    // Therefore, has to be serialized differently.
    protected static boolean readSpecificField(ResourceContext context, String value) {
        if (context.getPath().getLast().equals("div")) {
            Property property = context.getResource().getNamedProperty(navigatePath(context.getPath(), 1));
            if ("Narrative" .equals(property.getTypeCode()) && property.hasValues()) {
                Base base = property.getValues().get(0);
                Narrative narrative = base.castToNarrative(base);
                narrative.setDivAsString(value);
            }
            context.getPath().removeLast();
            return true;
        }
        return false;
    }

    private static Operation<String> findRegisteredNode(ResourceContext context) {
        Iterator<String> iterator = context.getPath().iterator();

        StringBuilder stringBuilder = new StringBuilder();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());

            if (context.getArrayContext().hasNode(stringBuilder.toString())) {
                return new Operation<>(stringBuilder.toString());
            } else {
                stringBuilder.append(".");
            }
        }

        String absolutePath = navigatePath(context.getPath(), true, context.getPath().size() - 1);
        if (context.getArrayContext().hasNode(absolutePath)) {
            return new Operation<>(absolutePath);
        }

        return new Operation<>();
    }

    private static Operation<List<String>> findAllRegisteredNodes(ResourceContext context) {
        Iterator<String> iterator = context.getPath().iterator();

        List<String> possibleNodes = new ArrayList<>();

        StringBuilder stringBuilder = new StringBuilder();
        while (iterator.hasNext()) {
            stringBuilder.append(iterator.next());

            if (context.getArrayContext().hasNode(stringBuilder.toString())) {
                possibleNodes.add(stringBuilder.toString());
            }
            stringBuilder.append(".");
        }

        String absolutePath = navigatePath(context.getPath(), true, context.getPath().size() - 1);
        if (context.getArrayContext().hasNode(absolutePath)) {
            possibleNodes.add(absolutePath);
        }

        return new Operation<>(possibleNodes);
    }
}