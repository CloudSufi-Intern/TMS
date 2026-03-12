package cloudsufi.nextgen.tms.exception;

/**
 * Exception thrown when the client sends an invalid request,
 * such as missing required parameters or malformed data.
 * @author Ansh Parnami
 */
public class BadRequestException extends RuntimeException {
    /**
     * @param message The detail message explaining why the request is bad
     */
    public BadRequestException(String message) {
        super(message);
    }
}
