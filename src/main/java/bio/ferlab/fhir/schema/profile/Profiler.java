package bio.ferlab.fhir.schema.profile;

import bio.ferlab.fhir.converter.Operation;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.definition.ComplexDefinition;
import bio.ferlab.fhir.schema.definition.specificity.ExtensionDefinition;
import bio.ferlab.fhir.schema.profile.exception.InvalidCardinalityException;
import bio.ferlab.fhir.schema.utils.Constant;
import bio.ferlab.fhir.schema.utils.SchemaUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.Opt;
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
                                    StructureDefinition profile,
                                    List<StructureDefinition> extensions,
                                    List<String> requiredProperties,
                                    Map<String, bio.ferlab.fhir.schema.definition.Property> properties) {
        verifyIfProfileCorrespondToResource(profile, complexDefinition);

        Map<String, StructureDefinition> extensionsMap = extensions.stream()
                .collect(Collectors.toMap(StructureDefinition::getName, Function.identity()));

        for (Element element : profile.getDifferential().getElement()) {
            Cardinality cardinality = new Cardinality(element);
            if (!cardinality.isValid()) {
                continue;
            }

            String elementName = SchemaUtils.parsePropertyName(element.getId());
            if (elementName.startsWith("extension:")) {
                String extensionName = elementName.replace("extension:", "");
                verifyIfExtensionIsProvided(extensionsMap, extensionName);
                StructureDefinition extension = extensionsMap.get(extensionName);
                applyExtension(extensionName, extension, cardinality);
            } else {
                if (cardinality.toBeRemoved()) {
                    removeProperty(elementName, properties);
                } else if (cardinality.isRequired()) {
                    requiredProperties.add(elementName);
                } else if (cardinality.isArray()) {
                    System.out.println("This is an array!");
                } else if (cardinality.isOptional()) {
                    System.out.println("This field is optional!");
                }
            }
        }
    }

    private static void applyExtension(String extensionName, StructureDefinition extension, Cardinality cardinality) {
        switch (extension.getKind()) {
            case PRIMITIVETYPE:
            case RESOURCE:
                Operation<String> operation = getExtensionType(extension);
                if (operation.isValid()) {
                    ExtensionDefinition.registerExtension(extensionName, operation.getResult(), cardinality.isRequired());
                }
                break;
            case COMPLEXTYPE:
                for (ElementDefinition elementDefinition : extension.getDifferential().getElement()) {
                    if (elementDefinition.getId().startsWith("Extension.extension:") && elementDefinition.getId().contains("value[x]")) {
                        String elementName = StringUtils.substringBetween(elementDefinition.getId(), "Extension.extension:", ".value[x]");
                        Operation<String> extensionType = readExtensionType(elementDefinition);
                        if (extensionType.isValid()) {
                            ExtensionDefinition.registerExtension(elementName, extensionType.getResult(), new Cardinality(elementDefinition).isRequired());
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
}
