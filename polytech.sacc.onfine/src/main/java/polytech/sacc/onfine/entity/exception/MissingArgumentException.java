package polytech.sacc.onfine.entity.exception;

public class MissingArgumentException extends Exception {
    public MissingArgumentException(String argumentName){
        super("Error: Missing argument [" + argumentName + "]");
    }
}
