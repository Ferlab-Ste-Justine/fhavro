package bio.ferlab.fhir.schema.profile;

import bio.ferlab.fhir.converter.Operation;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.definition.ComplexDefinition;
import bio.ferlab.fhir.schema.definition.Property;
import bio.ferlab.fhir.schema.definition.SchemaDefinition;
import bio.ferlab.fhir.schema.definition.specificity.ExtensionDefinition;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.SchemaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.r4.model.Element;
import org.hl7.fhir.r4.model.ElementDefinition;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bio.ferlab.fhir.converter.ConverterUtils.getBase;

public class Profiler {

    private Profiler() {
    }

    public static void applyProfile(ComplexDefinition complexDefinition,
                                    SchemaDefinition schemaDefinition,
                                    List<String> requiredProperties,
                                    Map<String, bio.ferlab.fhir.schema.definition.Property> properties) {
        verifyIfProfileCorrespondToResource(schemaDefinition.getProfile(), complexDefinition);

        Map<String, StructureDefinition> extensionsMap = schemaDefinition
                .getExtensions()
                .stream()
                .collect(Collectors.toMap(StructureDefinition::getId, Function.identity()));

        properties.put("extension", createExtensionProperty());

        for (Element element : schemaDefinition.getProfile().getDifferential().getElement()) {
            String elementName = SchemaUtils.parsePropertyName(element.getId());

            Cardinality cardinality = new Cardinality(element);
            if (!cardinality.isValid()) {
                continue;
            }

            if (elementName.startsWith("extension:")) {
                String extensionName = "StructureDefinition/" + elementName.replace("extension:", "");

                verifyIfExtensionIsProvided(extensionsMap, extensionName);

                applyExtension(extensionsMap.get(extensionName));
            } else {
                if (cardinality.toBeRemoved()) {
                    removeProperty(elementName, properties);
                } else if (cardinality.isRequired()) {
                    requiredProperties.add(elementName);
                } else if (cardinality.isArray()) {
                    // TODO find examples where this apply.
                    System.out.println("This is an array!");
                } else if (cardinality.isOptional()) {
                    // TODO find examples where this apply as well.
                    System.out.println("This field is optional!");
                }
            }
        }
    }

    private static void applyExtension(StructureDefinition extension) {
        switch (extension.getKind()) {
            case PRIMITIVETYPE:
            case RESOURCE:
                Operation<String> operation = getExtensionType(extension);
                if (operation.isValid()) {
                    ExtensionDefinition.registerExtension(operation.getResult());

                    setRecursivityDepth(1);
                }
                break;
            case COMPLEXTYPE:
                for (ElementDefinition elementDefinition : extension.getDifferential().getElement()) {
                    if (elementDefinition.getId().startsWith("Extension.extension:") && elementDefinition.getId().contains("value[x]")) {
                        Operation<String> extensionType = readExtensionType(elementDefinition);
                        if (extensionType.isValid()) {
                            ExtensionDefinition.registerExtension(extensionType.getResult());

                            setRecursivityDepth(2);
                        }
                    }
                }
                break;
            case LOGICAL:
            case NULL:
                throw new NotImplementedException();
        }
    }

    private static Operation<String> getExtensionType(StructureDefinition extension) {
        ElementDefinition extensionValue = extension.getDifferential().getElement().stream()
                .filter(x -> x.getId().startsWith("Extension.value"))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("An Extension element is missing its value[x] definition."));
        return readExtensionType(extensionValue);
    }

    private static Operation<String> readExtensionType(ElementDefinition elementDefinition) {
        return new Operation<>(getBase(getBase(elementDefinition
                .getNamedProperty(Constant.TYPE).getValues())
                .getNamedProperty(Constant.CODE).getValues())
                .primitiveValue());
    }

    private static void removeProperty(String elementName, Map<String, bio.ferlab.fhir.schema.definition.Property> properties) {
        if (elementName.contains("[x]")) {
            String formattedElementName = elementName.replace("[x]", "");
            properties.keySet().stream()
                    .filter(x -> x.startsWith(formattedElementName))
                    .collect(Collectors.toList())
                    .forEach(properties::remove);
        } else {
            properties.remove(elementName);
        }
    }

    private static Property createExtensionProperty() {
        ObjectMapper objectMapper = new ObjectMapper();
        return new Property(objectMapper.createObjectNode()
                .put("description", "May be used to represent additional information that is not part of the basic definition of the element. To make the use of extensions safe and manageable, there is a strict set of governance  applied to the definition and use of extensions. Though any implementer can define an extension, there is a set of requirements that SHALL be met as part of the definition of the extension.")
                .put("type", "array")
                .set("items", objectMapper.createObjectNode().put("$ref", "#/definitions/Extension")));
    }

    private static void verifyIfProfileCorrespondToResource(StructureDefinition profile, ComplexDefinition complexDefinition) {
        String baseDefinitionName = SchemaUtils.parseBaseDefinition(profile.getBaseDefinition());
        String complexDefinitionName = complexDefinition.getName();
        if (!baseDefinitionName.equals(complexDefinitionName)) {
            throw new BadRequestException(String.format("The following Definition: %s does not correspond to the provided Profile: %s", complexDefinitionName, baseDefinitionName));
        }
    }

    private static void verifyIfExtensionIsProvided(Map<String, StructureDefinition> extensionsMap, String extensionName) {
        if (!extensionsMap.containsKey(extensionName)) {
            throw new BadRequestException("Please provide the Extension file for: " + extensionName);
        }
    }

    private static void setRecursivityDepth(int value) {
        int depth = DefinitionRepository.getRecursivityDepth();
        if (value > depth) {
            DefinitionRepository.setRecursivityDepth(value);
        }
    }
}
