package bio.ferlab.fhir.schema;

import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.cli.*;
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
            helpFormatter.printHelp("--generate <fullname>", options);
            System.exit(1);
            return;
        }

        initialize();

        generate(commandLine.getOptionValue("generate"));

        if (commandLine.hasOption("report"))
            LOGGER.info(String.format("Report:%n" +
                    "--- Number of support entities: %d%n" +
                    "--- Number of unsupported entities: %d%nSupported entities: %s", supportedEntities.size(), unsupportedEntities.size(), supportedEntities));
    }

    private static void setupCommandLine() {
        Option option = new Option("g", "generate", true, "Generate the schema for the specific entity by its fullname.");
        option.setRequired(true);
        option.setArgName("fullname");
        options.addOption(option);
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
    }

    public static void generate(String identifier) {
        if ("all" .equalsIgnoreCase(identifier)) {
            generatedEntities = new ArrayList<>(DefinitionRepository.getComplexDefinitions().keySet());
            loadAll();
        } else {
            loadOne(identifier);
        }

        LOGGER.info("Schema generated: " + identifier);
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

                loadOne(currentKey);
                listIterator.remove();
            }
        } catch (StackOverflowError stackOverflowError) {
            unsupportedEntities.add(currentKey);
            loadAll();
        }
    }

    public static void loadOne(String identifier) {
        if (DefinitionRepository.generateDefinition(identifier)) {
            supportedEntities.add(identifier);
            LOGGER.info("Generated: " + identifier);
        }
    }
}
