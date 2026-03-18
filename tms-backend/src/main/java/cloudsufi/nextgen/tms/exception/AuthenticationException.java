package cloudsufi.nextgen.tms.exception;

/**
 * Exception thrown when a user or service fails authentication checks
 *
 * @author Vedanshu Garg
 */
public class AuthenticationException extends RuntimeException {
    /**
     * @param message The detail message explaining the authentication failure
     */
    public AuthenticationException(String message) {
        super(message);
    }
}
