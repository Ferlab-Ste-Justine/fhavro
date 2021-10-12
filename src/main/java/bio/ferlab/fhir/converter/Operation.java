package bio.ferlab.fhir.converter;

// Java does support out parameter like in C#, this is a class to emulate that.
public class Operation<T> {
    private boolean valid;
    private T result;

    public Operation() {
        valid = false;
    }

    public Operation(T t) {
        valid = true;
        result = t;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
