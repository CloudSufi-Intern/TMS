package cloudsufi.nextgen.tms.exception;

import cloudsufi.nextgen.tms.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler that intercepts application exceptions and
 * translates them into structured HTTP responses
 *
 * @author Vedanshu Garg
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException and returns 404 Not Found status
     *
     * @param ex        The intercepted ResourceNotFoundException
     * @param request   The current HTTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 404 status
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("ResourceNotFoundException handled: {}", ex.getMessage());
        return createErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Handles DuplicationResourceException and returns 409 Conflict status
     *
     * @param ex        The intercepted DuplicateResourceException
     * @param request   The current HHTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 409 status
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("DuplicateResourceException handled: {}", ex.getMessage());
        return createErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    /**
     * Handles AuthenticationException and returns 401 Unauthorized status
     *
     * @param ex        The intercepted AuthenticationException
     * @param request   The current HTTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 401 status
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        log.warn("AuthenticationException handled: {}", ex.getMessage());
        return createErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Handles InvalidTokenException and returns 401 Unauthorized status
     *
     * @param ex        The intercepted InvalidTokenException
     * @param request   The current HTTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 401 status
     */
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidTokenException(InvalidTokenException ex, HttpServletRequest request) {
        log.warn("InvalidTokenException handled: {}", ex.getMessage());
        return createErrorResponse(ex, HttpStatus.UNAUTHORIZED, request);
    }

    /**
     * Fallback handler for any unexpected exception, returning 500 Internal Server Error status
     *
     * @param ex        The intercepted generic exception
     * @param request   The current HTTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled application exception: ", ex);

        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Utility method for mapping standard attributes to the JSON DTO mapping structure
     *
     * @param ex        The exception object.
     * @param status    The mapped HttpStatus
     * @param request   The current HTTP request
     * @return          An instantiation of the unified ErrorResponse record standard
     */
    private ResponseEntity<ErrorResponseDTO> createErrorResponse(Exception ex, HttpStatus status, HttpServletRequest request) {
        ErrorResponseDTO errorResponse = ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return new ResponseEntity<>(errorResponse, status);
    }
}
