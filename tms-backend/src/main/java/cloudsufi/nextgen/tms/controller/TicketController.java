package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.TicketRaiseRequest;
import cloudsufi.nextgen.tms.dto.TicketRaiseResponse;
import cloudsufi.nextgen.tms.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * REST Controller for managing ticket-related operations in the Task Management System.
 * Provides endpoints to retrieve and raise tickets.
 *
 * @author Ansh Parnami
 */
@RestController
@RequestMapping("/api/tickets") // Standard API versioning path
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketRaiseResponse> raiseTicket(@ModelAttribute TicketRaiseRequest request) {
        log.info("REST request received to raise a new ticket with title: '{}'", request.getTitle());
        TicketRaiseResponse response = ticketService.raiseTicket(request);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}