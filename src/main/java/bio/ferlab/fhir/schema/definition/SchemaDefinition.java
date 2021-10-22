package bio.ferlab.fhir.schema.definition;

import bio.ferlab.fhir.FhavroConverter;
import bio.ferlab.fhir.converter.exception.BadRequestException;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.StructureDefinition;

import java.util.ArrayList;
import java.util.List;

public class SchemaDefinition {

    private String schemaName;
    private StructureDefinition profile;
    private List<StructureDefinition> extensions;

    public SchemaDefinition() {
        extensions = new ArrayList<>();
    }

    public SchemaDefinition(String schemaName) {
        this();
        if (StringUtils.isBlank(schemaName)) {
            throw new BadRequestException("Please verify the following schema name: " + schemaName);
        }

        setSchemaName(schemaName);
    }

    public SchemaDefinition(String schemaName, String profileName, String[] extensionNames) {
        this(schemaName);
        if (StringUtils.isNotBlank(profileName)) {
            setProfile(FhavroConverter.loadProfile(profileName));
            if (extensionNames != null && extensionNames.length > 0) {
                for (String extensionName : extensionNames) {
                    extensions.add(FhavroConverter.loadExtension(extensionName));
                }
            }
        }
    }

    public SchemaDefinition(String schemaName, StructureDefinition profile, List<StructureDefinition> extensions) {
        this(schemaName);
        setProfile(profile);
        setExtensions(extensions);
    }

    public boolean hasProfile() {
        return profile != null;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public StructureDefinition getProfile() {
        return profile;
    }

    public void setProfile(StructureDefinition profile) {
        this.profile = profile;
    }

    public List<StructureDefinition> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<StructureDefinition> extensions) {
        this.extensions = extensions;
    }
}
