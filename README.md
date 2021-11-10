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
// Load the schema from the packaged schema in the library.
Schema patientSchema = FhavroConverter.loadSchema("Patient", SchemaMode.DEFAULT);

// Use this method if you want to cache the schema locally in a file
String patientSchemaString = FhavroConverter.generateSchema("Patient", SchemaMode.DEFAULT);
```

### Fhir → Avro
With the schema, you can convert the Fhir resource to its Avro counterpart, like so:
```java
Schema schema = FhavroConverter.loadSchema("Patient", SchemaMode.DEFAULT);

Patient patient = new Patient();
patient.setGender(Enumerations.AdministrativeGender.MALE);
patient.setBirthDate(new Date());
patient.setActive(true);
patient.addName()
       .addGiven("Homer")
       .setFamily("Simpson");

GenericRecord patientRecord = FhavroConverter.convertResourceToGenericRecord(patient, schema);
```

The GenericRecord can be serialized as described in the following documentation: https://avro.apache.org/docs/current/gettingstartedjava.html

### Avro → Fhir
Again, with the schema, you can convert the Avro record to a Fhir resource, like so:
```java
Patient convertedPatient = FhavroConverter.convertGenericRecordToResource(patientRecord, schema, "Patient");
```

## Schema Generation

The source file used to generate the schemas can be found on the official [download](https://www.hl7.org/fhir/downloads.html) page of the FHIR infrastructure, 
but the latest version of it is packaged in the library itself.

![alt text](https://github.com/Ferlab-Ste-Justine/fhavro/blob/main/doc/images/schema_generation_process.png?raw=true)

The resulting Schema file (.avsc) are located under /src/resources/schemas/* of this project under their respective schema mode.
All default & simple schemas are packaged in the library itself.

### How to? (Command-Line interface)

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

### How to ? (FhavroConverter)

The FhavroConverter is java class that can be accessed without instantiation. It exposes static method in order to play with the library itself.

```
usage: FhavroConverter.generateSchema(String schemaName, SchemaMode schemaMode);
returns: A String corresponding to the Schema (.avsc)
```
e.g: 

```Java
StructureDefiniton profile = FhavroConverter.loadProfile(<InputStream>);
List<StructureDefinition> extensions = List.of(
        FhavroConverter.loadExtension(<InputStream>),
        FhavroConverter.loadExtension(<InputStream>)
);

String advancedPatientSchema = FhavroConverter.generateSchema("Patient", SchemaMode.ADVANCED, profile, extensions);
```

### Mode

Mode | Description | Extension 
--- | --- | ---
DEFAULT | This is the default way to generate the schemas. All the fields are supported as defined in the FHIR v4.0.7 specification | Yes
SIMPLE | Most fields are supported except for Extensions. Moreover, the cyclical definition between Reference & Identifier is not supported. | No
ADVANCED | Similar to Default except that Extension definition contains only primitive values (e.g: Boolean, String, etc.). Further Extension values must be explicitly defined using a Profile. | Yes, and more if explicitly defined 

### Profile

Some FHIR resources contains fields which starts with an underscore ('_'). Those fields are defined as Element (#/definitions/Element in fhir.schema.json)
Those fields are used to extend already existing fields by adding undeclared extensions. Fhavro can serialize them, however they must be included in the schema in order 
to be serialized.

Below is an example of Element (Default values have been removed for readability reasons).
```

    {
      "name": "receivedTime",
      "type": [
        "null",
        {
          "type": "long",
          "logicalType": "timestamp-micros"
        }
      ]
    },
    {
      "name": "_receivedTime", // Undeclared Extension of receivedTime, a timestamp.
      "type": {
        "type": "record",
        "name": "Element",
        "doc": "Base definition for all elements in a resource.",
        "fields": [
          { "name": "id", ... },
          {
            "name": "extension",
            "type": {
              "type": "array",
              "items": "bio.ferlab.fhir.Extension", // Extensions have already been defined by default.
            }
          }
        ]
      }
    }

```
Note: As of right now, you'd need to generate the schema and modify it manually.

## Serialization

```Java
// Load the Schema for your FHIR resource.
Schema schema = loadSchema(schemaName, schemaMode);

// Convert all your FHIR resource to GenericRecord
List<GenericRecord> genericRecords = resources.stream()
    .map(resource -> convertResourceToGenericRecord(resource, schema))
    .collect(Collectors.toList());

// Write your GenericRecords to an Avro file.
serializeGenericRecords(schema, genericRecords, new FileOutputStream("Patient.avro"));
```

## Deserialization

```Java

// Load the Schema for your FHIR resource
Schema schema = loadSchema(schemaName, schemaMode);

// Read the List of GenericRecords from the file.
List<GenericRecord> genericRecords = deserializeGenericRecords(schema, new File("Patient.avro"));

// Convert all your GenericRecord to FHIR Resource (You will need to typecast it to what you expect to read)
List<Patient> patients = genericRecords.stream()
        .map(genericRecord -> (Patient) convertGenericRecordToResource(genericRecord, schema, "Patient"))
        .collect(Collectors.toList());
```
