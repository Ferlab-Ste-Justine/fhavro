package bio.ferlab.fhir.schema;

import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.schema.definition.specificity.ExtensionDefinition;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class GenerateSchemas {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenerateSchemas.class);

    private static final List<String> unsupportedEntities = new ArrayList<>();
    private static final List<String> supportedEntities = new ArrayList<>();

    private static final Options options = new Options();
    private static final HelpFormatter helpFormatter = new HelpFormatter();
    private static final CommandLineParser commandLineParser = new DefaultParser();

    private static List<String> generatedEntities = new ArrayList<>();

    public static void main(String[] args) throws URISyntaxException, IOException {
        setupCommandLine();

        CommandLine commandLine;
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException e) {
            helpFormatter.printHelp("--generate <profileName>", options);
            System.exit(1);
            return;
        }

        initialize();

        generate(commandLine.getOptionValue("generate"),
                commandLine.getOptionValue("profile"),
                commandLine.getOptionValues("extension"));

        if (commandLine.hasOption("report")) {
            LOGGER.info(String.format("Report: %n" +
                    "--- Number of support entities: %d%n" +
                    "--- Number of unsupported entities: %d%nSupported entities: %s", supportedEntities.size(), unsupportedEntities.size(), supportedEntities));
        }
    }

    private static void setupCommandLine() {
        Option schema = new Option("g", "generate", true, "Generate the schema for the entity. The argument must be the fullname of the entity.");
        schema.setRequired(true);
        schema.setArgName("schemaName");
        options.addOption(schema);

        Option profile = new Option("p", "profile", true, "Generate the schema with a profile. The argument must be the filename of the profile (with extension).");
        profile.setRequired(false);
        profile.setArgName("profileName");
        options.addOption(profile);

        Option extensions = new Option("e", "extension", true, "Comma-separated list of Extension filename dependencies for the profile.");
        extensions.setRequired(false);
        extensions.setArgName("extensionNames");
        extensions.setArgs(Option.UNLIMITED_VALUES);
        options.addOption(extensions);

        Option report = new Option("r", "report", false, "Allow to output a report of the schema generation process or not.");
        report.setRequired(false);
        options.addOption(report);
    }

    private static void initialize() throws IOException, URISyntaxException {
        URL resource = ClassLoader.getSystemClassLoader().getResource("fhir.schema.json");
        if (resource == null) {
            throw new IllegalArgumentException("file not found!");
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(resource.toURI()));
        DefinitionRepository.populatePrimitiveDefinitions(root);
        DefinitionRepository.populateComplexDefinitions(root);
        ExtensionDefinition.initializeExtensions();
    }

    public static void generate(String schemaName, String profileName, String[] extensionNames) {
        if ("all" .equalsIgnoreCase(schemaName)) {
            generatedEntities = new ArrayList<>(DefinitionRepository.getComplexDefinitions().keySet());
            loadAll();

            LOGGER.info("Generated all schemas.");
        } else {
            loadOne(schemaName, profileName, extensionNames);
        }
    }

    public static void loadAll() {
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
    }

    public static void loadOne(String schemaName, String profileName, String[] extensionNames) {
        StructureDefinition profile = null;
        List<StructureDefinition> extensions = new ArrayList<>();
        if (StringUtils.isNotBlank(profileName)) {
            profile = FhavroConverter.loadProfile(profileName);
            if (extensionNames != null) {
                for (String filename : extensionNames) {
                    extensions.add(FhavroConverter.loadExtension(filename));
                }
            }
        }

        if (DefinitionRepository.generateDefinition(schemaName, profile, extensions)) {
            supportedEntities.add(schemaName);
            if (profile != null) {
                LOGGER.info(String.format("Generated: %s with %s profile", schemaName, profileName));
            } else {
                LOGGER.info(String.format("Generated: %s", schemaName));
            }
        }
    }
}
