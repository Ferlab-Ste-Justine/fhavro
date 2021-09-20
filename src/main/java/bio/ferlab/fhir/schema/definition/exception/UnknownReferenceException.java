package bio.ferlab.fhir.schema.definition.exception;

public class UnknownReferenceException extends RuntimeException {

    public UnknownReferenceException(String reference) {
        super("The following reference object is unknown OR this behaviour is not yet implemented. Please verify: " + reference);
    }
}
