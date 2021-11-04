package bio.ferlab.fhir.converter;

import bio.ferlab.fhir.converter.exception.AvroConversionException;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.converter.exception.LogicException;
import bio.ferlab.fhir.converter.exception.UnionTypeException;
import bio.ferlab.fhir.schema.utils.Constant;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.util.TerserUtilHelper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericArray;
import org.apache.avro.generic.GenericRecord;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.r4.model.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static bio.ferlab.fhir.converter.ConverterUtils.navigatePath;

public class AvroFhirConverter {

    private static final FhirContext fhirContext;

    AvroFhirConverter() {
    }

    static {
        fhirContext = FhirContext.forR4();
    }

    public static <T extends BaseResource> T readGenericRecord(GenericRecord genericRecord, Schema schema, String name) {
        ResourceContext resourceContext = new ResourceContext(TerserUtilHelper.newHelper(fhirContext, name));
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

            if (innerField.name().startsWith("_")) {
                context.addLastToPath(innerField.name());
                readUndeclaredExtensions(context, genericRecord.get(innerField.name()));
                context.getPath().removeLast();
                continue;
            }

            if (context.isElement(innerField.name())) {
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

        if (context.isElement(absolutePath)) {
            context.getPath().removeLast();
            return;
        }

        // Build the Array elements.
        for (Object element : (List<?>) value) {
            Operation<List<String>> operation = context.findAllRegisteredNodes();
            if (operation.isValid()) {
                for (String node : operation.getResult()) {
                    Base currentBase = (Base) context.getArrayContext().getCurrentBase(node);
                    String relativePath = navigatePath(context.getPath(), true, context.getPath().size(), ConverterUtils.getSkipCount(node));
                    if (addElement(context, currentBase, relativePath, element)) {
                        break;
                    }
                }
            } else {
                context.getTerser().addElement(context.getResource(), absolutePath);
            }
        }

        context.detectPathConflict();

        // Populate the Array.
        for (Object element : (List<?>) value) {
            read(context, field, schema.getElementType(), element);

            context.getArrayContext().progressNode(absolutePath);
            context.addLastToPath(field.name());
        }

        context.getPath().removeLast();
    }

    private static boolean addElement(ResourceContext context, Base base, String relativePath, Object element) {
        try {
            if (element instanceof GenericRecord) {
                context.getTerser().addElement(base, relativePath);
            } else {
                context.getTerser().addElement(base, relativePath, element.toString());
            }
            return true;
        } catch (DataFormatException ex) {
            return false;
        }
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
            case Constant.TIMESTAMP_MILLIS:
                readType(context, DateUtils.formatTimestampMillis((Long) value));
                return;
            case Constant.TIMESTAMP_MICROS:
                if (String.valueOf(value).length() <= 5) {
                    readType(context, DateUtils.formatDate(Math.toIntExact((Long) value)));
                } else {
                    readType(context, DateUtils.formatTimestampMicros((Long) value));
                }
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
        if (!context.getArrayContext().hasNode(root)) {
            Operation<List<String>> operation = context.findAllRegisteredNodes();
            if (operation.isValid()) {
                for (String node : operation.getResult()) {
                    Base base = (Base) context.getArrayContext().getCurrentBase(node);
                    if (base.isPrimitive() && Constant.STRING.equals(base.fhirType())) {
                        continue;
                    }

                    setValue(context,
                            base,
                            navigatePath(context.getPath(), true, context.getPath().size(), ConverterUtils.getSkipCount(node)),
                            value);
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
            if ("Narrative".equals(property.getTypeCode()) && property.hasValues()) {
                Base base = property.getValues().get(0);
                Narrative narrative = base.castToNarrative(base);
                narrative.setDivAsString(value);
            }
            context.getPath().removeLast();
            return true;
        }

        return false;
    }

    private static void setValue(ResourceContext context, Base parent, String relativePath, String value) {
        if (parent.isPrimitive()) {
            context.getHelper().setField(navigatePath(context.getPath()), value);
            return;
        }

        try {
            List<IBase> elements = context.getTerser().getValues(parent, relativePath);
            if (elements.isEmpty()) {
                context.getTerser().setElement(parent, relativePath, value);
            }
        } catch (ClassCastException ignored) {
        }
    }

    private static void readUndeclaredExtensions(ResourceContext context, Object value) {
        List<GenericRecord> values = new ArrayList<>();
        if (value instanceof GenericRecord) {
            values = (List<GenericRecord>) ((GenericRecord) value).get(Constant.EXTENSION);
        } else if (value instanceof GenericArray) {
            values = (List<GenericRecord>) value;
        }

        if (values.isEmpty()) {
            return;
        }

        Base base = context.getTerser().addElement(context.getResource(), navigatePath(context.getPath()).replace("_", ""));
        for (GenericRecord element : values) {
            ((PrimitiveType) base).addExtension(ElementConverter.readExtension(element));
        }

        String root = context.getPath().stream()
                .filter(node -> node.startsWith("_"))
                .map(node -> node.replace("_", ""))
                .map(node -> node.split("(?<=[a-z])(?=[A-Z])"))
                .findFirst()
                .orElseThrow(LogicException::new)[0];
        context.registerElements(root);
    }
}