package cloudsufi.nextgen.tms.exception;

/**
 * Exception thrown when a provided security token is expired, malformed, or invalid.
 *
 * @author Vedanshu Garg
 */
public class InvalidTokenException extends RuntimeException {
    /**
     * @param message The detail message explaining why the token is invalid
     */
    public InvalidTokenException(String message) {
        super(message);
    }
}
