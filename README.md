FHAVRO
=========

FHAVRO - Java library for serialization/deserialization of HL7 FHIR v4.0.1 resources in Apache Avro.

## Documentation

FHIR is a standard for health care data exchange, published by HL7: https://hl7.org/FHIR/ 

HAPI FHIR is a complete implementation of the HL7 FHIR standard for healthcare interoperability in Java.: https://hapifhir.io/

Apache Avro is a data serialization system: https://avro.apache.org/docs/current/

## Getting Started

In order to convert between resources between Fhir <-> Avro, a Schema is necessary in order to dictate how to parse the said resource.
To do so, simply load the schema like so:

```java
String patientSchemaName = "Patient";
Schema patientSchema = FhavroConverter.loadSchema(patientSchemaName, SchemaMode.DEFAULT);
```

### Fhir → Avro
With the schema, you can convert the Fhir resource to its Avro counterpart, like so:
```java
Patient patient = new Patient();
patient.setGender(Enumerations.AdministrativeGender.MALE);
patient.setBirthDate(new Date());
patient.setActive(true);
patient.addName()
       .addGiven("Homer")
       .setFamily("Simpson");

GenericRecord patientRecord = FhavroConverter.convertResourceToGenericRecord(patient, patientSchema);
```

The GenericRecord can be serialized as described in the following documentation: https://avro.apache.org/docs/current/gettingstartedjava.html

### Avro → Fhir
Again, with the schema, you can convert the Avro record to a Fhir resource, like so:
```java
Patient convertedPatient = FhavroConverter.convertGenericRecordToResource(patientRecord, patientSchema, Patient.class);
```

## Architecture

### Schema Generation

The source file used to generate the schemas can be found on the official [download](https://www.hl7.org/fhir/downloads.html) page of the FHIR infrastructure.

![alt text](https://github.com/Ferlab-Ste-Justine/fhavro/blob/main/doc/images/schema_generation_process.png?raw=true)

The resulting Schema file (.avsc) are located under /src/resources/schemas/* of this project under their respective schema mode.
All default & simple schemas are packaged in the library itself.

#### Usage

```
usage: GenerateSchemas --generate <schemaName> --mode <mode>
```

Example of a schema:
```
GenerateSchemas --generate Appointment --mode Default
```

Example of a simple schema: 
```
GenerateSchemas --generate Account --mode Simple
```

Example of an advanced schema with Profile:
```
GenerateSchemas -g Patient -m Advanced --profile StructureDefinition-cqdg-patient.json --extension StructureDefinition-family-id.json StructureDefinition-is-fetus.json StructureDefinition-is-proband.json StructureDefinition-family-relation.json
```

Note: 
- The schemaName argument is case-sensitive and must be a resource supported here: https://www.hl7.org/fhir/resourcelist.html
- the mode argument is case-insensitive, but must be of value as described below.

#### Mode

Mode | Description | Extension 
--- | --- | ---
DEFAULT | This is the default way to generate the schemas. All the fields are supported as defined in the FHIR v4.0.7 specification | Yes
SIMPLE | Most fields are supported except for Extensions. Moreover, the cyclical definition between Reference & Identifier is not supported. | No
ADVANCED | Similar to Default except that Extension definition does not contain any value. Extension values must be explicitly defined using a Profile. | Only if explicitly defined 

## Known issues

 - "Private" fields are not serializable yet. (e.g: field where field name starts with "_" of type Element).
 - "Narrative.div" is marked as Optional even thought it is Required because some entities does not provide it (may be due to the quality of the dev data).