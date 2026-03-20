package cloudsufi.nextgen.tms.enums;

/**
 * Defines the allowed file types for ticket attachments.
 *
 * This restricts users from uploading potentially malicious files (like .exe or .sh)
 * by forcing the application to map the upload to one of these safe categories.
 * @author Ansh Parnami
 */
public enum FileType {
    /** Image files (e.g., PNG, JPG, JPEG) */
    IMAGE,

    /** PDF Documents */
    PDF,

}