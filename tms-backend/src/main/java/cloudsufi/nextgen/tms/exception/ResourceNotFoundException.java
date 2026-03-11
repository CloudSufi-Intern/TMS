package cloudsufi.nextgen.tms.exception;

/**
 * Exception thrown when a requested resource does not exist
 *
 * @author Vedanshu Garg
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * @param message The detail message explaining which resource was missing
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
