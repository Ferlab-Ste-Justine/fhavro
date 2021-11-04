package bio.ferlab.fhir.converter.exception;

// Exception that represents error in the program logic.
public class LogicException extends RuntimeException {

    public LogicException() {
        super("Please verify this logic, this behaviour should never occur.");
    }
}
