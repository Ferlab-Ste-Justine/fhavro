package bio.ferlab.fhir.schema;

import bio.ferlab.fhir.converter.Operation;
import bio.ferlab.fhir.schema.definition.BaseDefinition;
import bio.ferlab.fhir.schema.definition.SchemaDefinition;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class GenerateSchemas {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateSchemas.class);

    private static final List<String> unsupportedEntities = new ArrayList<>();
    private static List<String> generatedEntities = new ArrayList<>();

    public static void main(String[] args) {
        CommandLine commandLine = setupCommandLine(args);

        DefinitionRepository.initialize(SchemaMode.parseSchemaMode(commandLine.getOptionValue("mode")));

        generate(commandLine.getOptionValue("generate"),
                commandLine.getOptionValue("profile"),
                commandLine.getOptionValues("extension"));
    }

    private static void generate(String schemaName, String profileName, String[] extensionNames) {
        if ("all".equalsIgnoreCase(schemaName)) {
            generatedEntities = new ArrayList<>(DefinitionRepository.getComplexDefinitions().keySet());
            loadAll();
        } else {
            loadOne(schemaName, profileName, extensionNames);
        }
    }

    private static void loadOne(String schemaName, String profileName, String[] extensionNames) {
        SchemaDefinition schema = new SchemaDefinition(schemaName, profileName, extensionNames);

        Operation<BaseDefinition> operation = DefinitionRepository.generateDefinition(schema);
        if (operation.isValid()) {

            saveDefinition(operation.getResult(), DefinitionRepository.getSchemaMode(), schema.hasProfile() ? schema.getProfile().getName().toLowerCase() : schemaName.toLowerCase());

            if (schema.hasProfile())
                LOGGER.info("Generated: {} with {} profile", schemaName, profileName);
            else {
                LOGGER.info("Generated: {}", schemaName);
            }
        }
    }

    private static void loadAll() {
        String currentKey = "";
        try {
            ListIterator<String> listIterator = generatedEntities.listIterator();
            while (listIterator.hasNext()) {
                currentKey = listIterator.next();
                if (unsupportedEntities.contains(currentKey)) {
                    continue;
                }
                loadOne(currentKey, null, null);
                listIterator.remove();
            }
        } catch (StackOverflowError stackOverflowError) {
            unsupportedEntities.add(currentKey);
            loadAll();
        }

        LOGGER.info("Generated all schemas.");
    }

    private static CommandLine setupCommandLine(String[] args) {
        Options options = new Options();

        Option schema = new Option("g", "generate", true, "Generate the schema for the entity. The argument must be the full name of the entity [Required].");
        schema.setRequired(true);
        schema.setArgName("schemaName");
        options.addOption(schema);

        Option mode = new Option("m", "mode", true, "Generate the schema with a mode (Default/Simple/Advanced)[Required].");
        mode.setRequired(true);
        mode.setArgName("schemaMode");
        options.addOption(mode);

        Option profile = new Option("p", "profile", true, "Generate the schema with a profile. The argument must be the filename of the profile (with extension) [Optional]");
        profile.setRequired(false);
        profile.setArgName("profileName");
        options.addOption(profile);

        Option extensions = new Option("e", "extension", true, "Space-separated list of Extension filename dependencies for the profile. [Optional]");
        extensions.setRequired(false);
        extensions.setArgName("extensionNames");
        extensions.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(extensions);

        try {
            return new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp("--generate <schemaName> --mode <mode>", options);
            System.exit(1);
        }
        throw new RuntimeException();
    }

    private static void saveDefinition(BaseDefinition baseDefinition, SchemaMode schemaMode, String filename) {
        if (baseDefinition.getDefinition().get("properties").has("resourceType")) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("./src/resources/schemas/" + schemaMode.toString().toLowerCase() + "/" + filename + ".avsc"))) {
                writer.write(baseDefinition.getJsonObject().toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
