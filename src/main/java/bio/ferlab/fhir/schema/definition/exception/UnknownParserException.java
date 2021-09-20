package bio.ferlab.fhir.schema.definition.exception;

public class UnknownParserException extends RuntimeException {

    public UnknownParserException(String identifier) {
        super("Unfortunately we do not know how the following property: " + identifier);
    }
}
