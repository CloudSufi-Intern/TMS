package cloudsufi.nextgen.tms.exception;

/*
 * Author: Smriti Bajpai
 *
 * Description:
 * Custom exception thrown when duplicate user information
 * (email or username) is detected.
 */

public class DuplicateUserException extends RuntimeException {

    public DuplicateUserException(String message) {
        super(message);
    }
}