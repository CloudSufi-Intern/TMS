package cloudsufi.nextgen.tms.exception;

/**
 * Custom exception thrown when the system fails to read, process,
 * or store a file attachment during ticket creation.
 * @author Ansh Parnami
 */
public class FileProcessingException extends RuntimeException {
    public FileProcessingException(String message) {
        super(message);
    }
}
