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

import java.util.List;

/**
 * REST Controller for managing ticket-related operations in the Task Management System.
 * Provides endpoints to retrieve and raise tickets.
 *
 * All endpoints require a valid JWT token — enforced by the security filter chain.
 *
 * @author Ansh Parnami
 */
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
public class TicketController {

    private final TicketService ticketService;

    /**
     * Endpoint to raise a new support ticket.
     * The user's identity is automatically extracted from their JWT token.
     *
     * @param request The ticket details (title, description, priority, attachments)
     * @return A success response with the new Ticket ID
     * @author Ansh Parnami
     */

    /**
     * Endpoint to raise a new support ticket.
     * * [Ticket Update]: Integrated strict request validation using @Valid.
     * Updated parameter binding to @ModelAttribute to correctly consume
     * multipart/form-data, allowing seamless frontend integration with file attachments.
     * * @param request The validated ticket details including optional files
     * @author Priyanshu Gupta
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketRaiseResponse> raiseTicket(@ModelAttribute TicketRaiseRequest request) {
        log.info("REST request received to raise a new ticket with title: '{}'", request.getTitle());
        TicketRaiseResponse response = ticketService.raiseTicket(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Retrieves the complete details of a specific ticket, including its history and attachments.
     * * @param id The ID of the ticket to retrieve.
     * @return 200 OK with the TicketDetailsResponse payload.
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketDetailsResponse> getTicketById(@PathVariable("id") Long id) {
        log.info("REST request to get Ticket ID: {}", id);
        TicketDetailsResponse response = ticketService.getTicketById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/tickets/my
     *
     * Returns all tickets where the authenticated user is either the creator (raiser)
     * or the assignee. Used to populate the dashboard page.
     *
     * The user's identity is resolved automatically from the JWT token via
     * the Spring Security context — no request parameters required.
     *
     * @return 200 OK with a list of {@link TicketResponseDTO}, empty list if none found.
     * @author Yashas Yadav
     */
    @GetMapping("/my")
    public ResponseEntity<List<TicketResponseDTO>> getMyTickets() {
        log.info("REST request received: GET /api/tickets/my");
        List<TicketResponseDTO> tickets = ticketService.getMyTickets();
        return ResponseEntity.ok(tickets);
    }


    /**
     * Partially updates a ticket.
     * Expects a JSON body with only the fields that need changing.
     */
    @PatchMapping("/{ticketId}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketUpdatePatchRequest request) {

        TicketResponseDTO updatedTicket = ticketService.updateTicket(ticketId, request);
        return ResponseEntity.ok(updatedTicket);
    }
    /**
     * POST /api/tickets/{ticketId}/comments
     * Adds a new comment to a ticket.
     *
     * @author Priyanshu Gupta
     */
    @PostMapping("/{ticketId}/comments")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long ticketId,
            @Valid @RequestBody CommentRequestDTO request) {
        log.info("REST request to add comment to Ticket ID: {}", ticketId);
        CommentResponseDTO response = ticketService.addComment(ticketId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * GET /api/tickets/{ticketId}/comments
     * Retrieves all comments for a ticket.
     *
     * @author Priyanshu Gupta
     */
    @GetMapping("/{ticketId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getComments(
            @PathVariable Long ticketId) {
        log.info("REST request to get comments for Ticket ID: {}", ticketId);
        List<CommentResponseDTO> comments = ticketService.getComments(ticketId);
        return ResponseEntity.ok(comments);
    }
}