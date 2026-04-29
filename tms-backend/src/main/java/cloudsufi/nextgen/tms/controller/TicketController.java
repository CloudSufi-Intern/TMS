package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.*;
import cloudsufi.nextgen.tms.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for ticket operations.
 * All endpoints require authentication (enforced by SecurityConfig).
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    /** Raise a new ticket (multipart, supports attachments). */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketRaiseResponse> raiseTicket(@ModelAttribute TicketRaiseRequest request) {
        log.info("Raising ticket: {}", request.getTitle());
        return new ResponseEntity<>(ticketService.raiseTicket(request), HttpStatus.CREATED);
    }

    /** Get full ticket detail. */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailsResponse> getTicketById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    /**
     * Tickets where the user is creator or assignee.
     * Supports sorting via {@code sortBy} (createdAt, updatedAt, priority,
     * status, title) and {@code sortDir} (asc, desc).
     */
    @GetMapping("/my")
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets(
            @RequestParam(required = false, defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(ticketService.getMyTickets(sortBy, sortDir));
    }

    /** Partial update — status / priority / assignee. */
    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketUpdatePatchRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(ticketId, request));
    }

    /**
     * Add a comment with optional attachments (multipart).
     * Frontend sends fields: content (text), attachments[] (files).
     */
    @PostMapping(value = "/{ticketId}/comments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommentResponseDTO> addCommentMultipart(
            @PathVariable Long ticketId,
            @RequestParam("content") String content,
            @RequestParam(value = "attachments", required = false) List<MultipartFile> attachments) {
        CommentRequestDTO dto = new CommentRequestDTO();
        dto.setContent(content);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.addComment(ticketId, dto, attachments));
    }

    /**
     * JSON fallback for adding a comment with no attachments. Kept so older
     * clients that POST application/json continue to work.
     */
    @PostMapping(value = "/{ticketId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CommentResponseDTO> addCommentJson(
            @PathVariable Long ticketId,
            @Valid @RequestBody CommentRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.addComment(ticketId, request, null));
    }

    /**
     * Get comments. Supports {@code sortDir=asc|desc} (default asc) and
     * {@code author=<username>} for filtering by comment author.
     */
    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getComments(
            @PathVariable Long ticketId,
            @RequestParam(required = false, defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String author) {
        return ResponseEntity.ok(ticketService.getComments(ticketId, sortDir, author));
    }
}
