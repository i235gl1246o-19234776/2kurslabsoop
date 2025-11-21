package exception;

public class FunctionException extends CustomException {
    public FunctionException(String message) {
        super(message, 500);
    }
}