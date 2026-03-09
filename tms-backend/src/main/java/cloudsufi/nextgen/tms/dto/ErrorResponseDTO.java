package cloudsufi.nextgen.tms.dto;

import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Standardized payload for HTTP error responses
 *
 * @author Vedanshu Garg
 * @param timestamp The exact date and time the error occurred
 * @param status    The HTTP status code
 * @param error     The error type
 * @param message   The error message explaining the issue
 * @param path      The API endpoint path
 */
@Builder
public record ErrorResponseDTO(
   LocalDateTime timestamp,
   int status,
   String error,
   String message,
   String path
) {}
