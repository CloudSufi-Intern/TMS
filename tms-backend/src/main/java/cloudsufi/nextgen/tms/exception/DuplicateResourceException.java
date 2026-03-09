package cloudsufi.nextgen.tms.exception;

/**
 * Exception thrown when attempting to create a resource that already exists
 *
 * @author Vedanshu Garg
 */
public class DuplicateResourceException extends RuntimeException {
    /**
     * @param message The detail message explaining the conflict
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
