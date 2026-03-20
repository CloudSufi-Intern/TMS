package cloudsufi.nextgen.tms.exception;

import cloudsufi.nextgen.tms.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

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
        return new ResponseEntity<>(createErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND, request), HttpStatus.NOT_FOUND);
    }

    /**
     * Handles DuplicationResourceException and returns 409 Conflict status
     *
     * @param ex        The intercepted DuplicateResourceException
     * @param request   The current HTTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 409 status
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicateResourceException(DuplicateResourceException ex, HttpServletRequest request) {
        log.warn("DuplicateResourceException handled: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorResponse(ex.getMessage(), HttpStatus.CONFLICT, request), HttpStatus.CONFLICT);
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
        return new ResponseEntity<>(createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request), HttpStatus.UNAUTHORIZED);
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
        return new ResponseEntity<>(createErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED, request), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handles @Valid bean validation failures and returns 400 Bad Request status.
     * Collects all field-level constraint violations into a single error message.
     *
     * @param ex        The intercepted MethodArgumentNotValidException
     * @param request   The current HTTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 400 status
     * @author Yashas Yadav
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("MethodArgumentNotValidException handled: {}", message);
        return new ResponseEntity<>(createErrorResponse(message, HttpStatus.BAD_REQUEST, request), HttpStatus.BAD_REQUEST);
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
        return new ResponseEntity<>(createErrorResponse("An unexpected error occurred. Please try again later.", HttpStatus.INTERNAL_SERVER_ERROR, request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles BadRequestException and returns 400 Bad Request status
     *
     * @param ex        The intercepted BadRequestException
     * @param request   The current HTTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 400 status
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequestException(BadRequestException ex, HttpServletRequest request) {
        log.warn("BadRequestException handled: {}", ex.getMessage());
        return new ResponseEntity<>(createErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST, request), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles FileProcessingException and returns 500 Internal Server Error status
     *
     * @param ex        The intercepted FileProcessingException
     * @param request   The current HTTP request
     * @return          ResponseEntity containing the structured ErrorResponse and 500 status
     */
    @ExceptionHandler(FileProcessingException.class)
    public ResponseEntity<ErrorResponseDTO> handleFileProcessingException(FileProcessingException ex, HttpServletRequest request) {
        log.error("FileProcessingException handled: ", ex);
        return new ResponseEntity<>(createErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, request), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Utility method for mapping standard attributes to the JSON DTO mapping structure
     *
     * @param message   The error message to be displayed
     * @param status    The mapped HttpStatus
     * @param request   The current HTTP request
     * @return          An instantiation of the unified ErrorResponse record standard
     */
    private ErrorResponseDTO createErrorResponse(String message, HttpStatus status, HttpServletRequest request) {
        return ErrorResponseDTO.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
    }
}
