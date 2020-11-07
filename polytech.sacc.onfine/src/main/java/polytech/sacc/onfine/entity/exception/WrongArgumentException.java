package polytech.sacc.onfine.entity.exception;

public class WrongArgumentException extends Exception {
    public WrongArgumentException(String argumentName){
        super("Error: Wrong argument [" + argumentName + "]");
    }
}
