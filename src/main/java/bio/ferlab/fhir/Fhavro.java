package bio.ferlab.fhir;

import bio.ferlab.fhir.converter.AvroFhirConverter;
import bio.ferlab.fhir.converter.ConverterUtils;
import bio.ferlab.fhir.converter.FhirAvroConverter;
import bio.ferlab.fhir.converter.Operation;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import bio.ferlab.fhir.schema.definition.BaseDefinition;
import bio.ferlab.fhir.schema.definition.SchemaDefinition;
import bio.ferlab.fhir.schema.repository.DefinitionRepository;
import bio.ferlab.fhir.schema.repository.SchemaMode;
import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.FhirContext;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Fhavro is an open-source library for the standardization and conversion of FHIR v4.0.1 data to the Apache Avro format.
 *
 * @author https://github.com/Ferlab-Ste-Justine
 */
public class Fhavro {

    private static final FhirContext fhirContext;

    Fhavro() {
    }

    static {
        fhirContext = FhirContext.forR4();
    }

    /**
     * Convert a FHIR v4.0.1 Resource in a Generic Record based on a predefined Schema.
     *
     * @param resource The FHIR resource to be converted.
     * @param schema   The Schema that represents the FHIR resource
     * @return The GenericRecord for this FHIR resource
     */
    @NotNull
    public static GenericData.Record convertResourceToGenericRecord(DomainResource resource, Schema schema) {
        return FhirAvroConverter.readResource(resource, schema);
    }

    /**
     * Convert a Generic Record representing a FHIR v4.0.1 Resource based on a predefined schema.
     *
     * @param genericRecord The GenericRecord representing a FHIR resource
     * @param schema        The Schema that represents the FHIR resource
     * @param name          The name of the FHIR resource (e.g: Patient, Account, etc).
     * @return The FHIR resource
     */
    @NotNull
    public static <T extends DomainResource> T convertGenericRecordToResource(GenericRecord genericRecord, Schema schema, String name) {
        return AvroFhirConverter.readGenericRecord(genericRecord, schema, name);
    }

    /**
     * Load the Schema by its name. The mode will determine where the schema will be fetched from.
     * If the mode is DEFAULT or SIMPLE, the schema will be searched within the Resources root folder of this library.
     * Else, if the mode is ADVANCED, the schema is searched from the Root of the calling project by its name.
     * <p>
     * SchemaMode:
     * <p>SIMPLE/DEFAULT: ./src/resources/schemas/'schemaMode'/'schemaName'
     * <p>ADVANCED: ./'schemaName'
     *
     * @param schemaName The name of the schema.
     * @param schemaMode The Schema Mode dictating where to search
     * @return The Schema
     */
    @NotNull
    public static Schema loadSchema(String schemaName, SchemaMode schemaMode) {
        schemaName = ConverterUtils.formatSchemaName(schemaName);
        return (schemaMode == SchemaMode.ADVANCED) ? loadSchemaFromRelativePath(schemaName) : loadSchemaFromResources(schemaName, schemaMode);
    }

    /**
     * Load the Schema from a given classpath resource.
     *
     * @param resource Classpath of the schema to load
     * @return The Schema
     */
    public static Schema loadSchemaFromResources(String resource) {
        try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource)) {
            return new Schema.Parser().parse(inputStream);
        } catch(IOException ex) {
            throw new BadRequestException(String.format("The following schema is not found within our resources: %s", resource));
        }
    }

    /**
     * Load an Extension from the Resource folder.
     *
     * @param filename The name of the extension
     * @return The Structure Definition of the Extension
     */
    @NotNull
    public static StructureDefinition loadExtension(String filename) {
        return loadProfile("extensions/" + filename);
    }

    /**
     * Load an Extension from an InputStream
     *
     * @param inputStream The InputStream where to load the Extension from.
     * @return The Structure Definition of the Extension
     */
    @NotNull
    public static StructureDefinition loadExtension(InputStream inputStream) {
        return loadProfile(inputStream);
    }

    /**
     * Load a Profile from the Resource folder.
     *
     * @param filename The name of the profile
     * @return The Structure Definition of the Profile
     */
    @NotNull
    public static StructureDefinition loadProfile(String filename) {
        try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("profiles/" + filename)) {
            return loadProfile(inputStream);
        } catch (Exception ex) {
            throw new BadRequestException("The following profile is not found: profiles/" + filename);
        }
    }

    /**
     * Load a Profile from an InputStream
     *
     * @param inputStream The InputStream where to load the Profile from.
     * @return The Structure Definition of the Profile
     */
    @NotNull
    public static StructureDefinition loadProfile(InputStream inputStream) {
        try (inputStream) {
            return fhirContext.newJsonParser().parseResource(StructureDefinition.class, IOUtils.toString(inputStream, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    /**
     * Generate a Schema for a FHIR v4.0.1 Resource as specified by its name.
     * The mode of generation is determine what characteristics the schema will have.
     * <p>
     * SchemaMode:
     * <p>DEFAULT: All the fields are supported as defined in the FHIR v4.0.7 specification
     * <p>SIMPLE: Most fields are supported except for Extensions. Moreover, the cyclical definition between Reference and Identifier is not supported.
     * <p>ADVANCED: Similar to Default except that Extension definition contains only primitive values (e.g: Boolean, String, etc.). Further Extension values must be explicitly defined using a Profile.
     *
     * @param schemaName The name of the schema representing a resource as represented in FHIR specification (e.g: Patient, Account, etc).
     * @param schemaMode The mode of generation for this Schema.
     * @return The String representation of this Schema.
     */
    @NotNull
    public static String generateSchema(String schemaName, SchemaMode schemaMode) {
        return generateDefinition(schemaMode, new SchemaDefinition(schemaName));
    }

    /**
     * Generate a Schema for a FHIR v4.0.1 Resource as specified by its name.
     * The mode of generation is determine what characteristics the schema will have.
     * <p>
     * SchemaMode:
     * <p>DEFAULT: All the fields are supported as defined in the FHIR v4.0.7 specification
     * <p>SIMPLE: Most fields are supported except for Extensions. Moreover, the cyclical definition between Reference and Identifier is not supported.
     * <p>ADVANCED: Similar to Default except that Extension definition contains only primitive values (e.g: Boolean, String, etc.). Further Extension values must be explicitly defined using a Profile.
     *
     * @param schemaName The name of the schema representing a resource as represented in FHIR specification (e.g: Patient, Account, etc).
     * @param schemaMode The mode of generation for this Schema.
     * @param profile    The profile to modify the schema with.
     * @param extensions The extensions that the profile depends on.
     * @return The String representation of this Schema.
     */
    @NotNull
    public static String generateSchema(String schemaName, SchemaMode schemaMode, StructureDefinition profile, List<StructureDefinition> extensions) {
        return generateDefinition(schemaMode, new SchemaDefinition(schemaName, profile, extensions));
    }

    /**
     * Serialize a List of Generic Record into an OutputStream.
     *
     * @param schema         The schema representing the generic records
     * @param genericRecords The list of generic records to be serialized
     * @param outputStream   The output stream where to serialize the list
     */
    public static void serializeGenericRecords(Schema schema, List<GenericRecord> genericRecords, OutputStream outputStream) {
        try (DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<>(new GenericDatumWriter<>(schema))) {
            dataFileWriter.create(schema, outputStream);
            for (GenericRecord genericRecord : genericRecords) {
                dataFileWriter.append(genericRecord);
            }
        } catch (IOException ex) {
            throw new BadRequestException("Please verify the OutputStream.");
        }
    }

    /**
     * Deserialize a File containing a List of Generic Record.
     * The schema must represent the type of the records serialized within.
     *
     * @param schema The schema representing the generic records.
     * @param file   The File where the generic records is saved within.
     * @return The List of Generic Record
     */
    public static List<GenericRecord> deserializeGenericRecords(Schema schema, File file) {
        DatumReader<GenericRecord> userDatumReader = new GenericDatumReader<>(schema);
        try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<>(file, userDatumReader)) {
            List<GenericRecord> list = new ArrayList<>();
            while (dataFileReader.hasNext()) {
                list.add(dataFileReader.next());
            }
            return list;
        } catch (IOException ex) {
            throw new BadRequestException("Please verify the Input File.");
        }
    }

    /**
     * Parse a FHIR v4.0.1 resource as represented by a JSON string back to its FHIR resource.
     *
     * @param resourceName The name of the resource (e.g: Patient, Account, etc).
     * @param json         The JSON string of the FHIR resource
     * @param <T>          The Type of FHIR resource to serialized back into.
     * @return The FHIR resource
     */
    public static <T extends BaseResource> T parseJsonResource(String resourceName, String json) {
        try {
            BaseRuntimeElementDefinition<IBaseResource> elementDefinition = fhirContext.getResourceDefinition(resourceName);
            return (T) fhirContext.newJsonParser().parseResource(elementDefinition.getImplementingClass(), json);
        } catch (Exception ex) {
            throw new BadRequestException(ex.getMessage());
        }
    }

    private static String generateDefinition(SchemaMode schemaMode, SchemaDefinition schemaDefinition) {
        DefinitionRepository.initialize(schemaMode);

        Operation<BaseDefinition> operation = DefinitionRepository.generateDefinition(schemaDefinition);
        if (operation.isValid()) {
            return operation.getResult().getJsonObject().toString();
        } else {
            throw new BadRequestException("The following schema is not found: " + schemaDefinition.getSchemaName());
        }
    }

    private static Schema loadSchemaFromResources(String schemaName, SchemaMode schemaMode) {
        String relativePath = String.format("schemas/%s/%s", schemaMode.toString().toLowerCase(), schemaName);
        return loadSchemaFromResources(relativePath);
    }

    private static Schema loadSchemaFromRelativePath(String schemaName) {
        String relativePath = String.format("./%s", schemaName);
        try (InputStream inputStream = new FileInputStream(relativePath)) {
            return new Schema.Parser().parse(inputStream);
        } catch(IOException ex) {
            throw new BadRequestException(String.format("The following schema is not found at: ./%s\n" +
                    "Ensure that the schema file (.avsc) is located at the relative path provided.", schemaName));
        }
    }
}
