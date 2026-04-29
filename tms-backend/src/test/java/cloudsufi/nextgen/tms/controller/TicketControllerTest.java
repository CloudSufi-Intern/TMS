package cloudsufi.nextgen.tms.controller;

import cloudsufi.nextgen.tms.dto.*;
import cloudsufi.nextgen.tms.enums.ApprovalStatus;
import cloudsufi.nextgen.tms.enums.Priority;
import cloudsufi.nextgen.tms.enums.Status;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.service.TicketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for TicketController.
 * Security filters are disabled to focus on HTTP routing and service delegation.
 *
 * @author Yashas Yadav
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // =========================================================
    // GET /api/tickets/{id}
    // =========================================================

    @Test
    @DisplayName("GET /api/tickets/{id} - Should return 200 with ticket details")
    void getTicketById_whenFound_shouldReturn200() throws Exception {
        TicketDetailsResponse response = TicketDetailsResponse.builder()
                .id(1L)
                .title("Fix login bug")
                .description("Login fails on mobile")
                .priority(Priority.HIGH)
                .status(Status.OPEN)
                .createdBy("yashascs")
                .assignedTo("Unassigned")
                .approver("N/A")
                .approvalStatus(ApprovalStatus.NOT_REQUIRED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .attachments(Collections.emptyList())
                .history(Collections.emptyList())
                .build();

        when(ticketService.getTicketById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Fix login bug"))
                .andExpect(jsonPath("$.createdBy").value("yashascs"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("GET /api/tickets/{id} - Should return 404 when ticket not found")
    void getTicketById_whenNotFound_shouldReturn404() throws Exception {
        when(ticketService.getTicketById(999L))
                .thenThrow(new ResourceNotFoundException("Ticket not found with ID: 999"));

        mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // GET /api/tickets/my
    // =========================================================

    @Test
    @DisplayName("GET /api/tickets/my - Should return 200 with list of tickets")
    void getMyTickets_shouldReturn200WithList() throws Exception {
        TicketResponseDTO ticket = TicketResponseDTO.builder()
                .id(1L)
                .title("Deploy to prod")
                .status(Status.IN_PROGRESS)
                .priority(Priority.URGENT)
                .createdBy("yashascs")
                .approvalStatus(ApprovalStatus.NOT_REQUIRED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .commentCount(2)
                .attachmentCount(0)
                .build();

        when(ticketService.getMyTickets(anyString(), anyString())).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("GET /api/tickets/my - Should return 200 with empty list when user has no tickets")
    void getMyTickets_whenNoTickets_shouldReturnEmptyList() throws Exception {
        when(ticketService.getMyTickets(anyString(), anyString())).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/tickets/my"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // =========================================================
    // PATCH /api/tickets/{ticketId}
    // =========================================================

    @Test
    @DisplayName("PATCH /api/tickets/{id} - Should return 200 on successful status update")
    void updateTicket_whenValid_shouldReturn200() throws Exception {
        TicketUpdatePatchRequest request = new TicketUpdatePatchRequest();
        request.setStatus(Status.IN_PROGRESS);

        TicketResponseDTO updated = TicketResponseDTO.builder()
                .id(1L)
                .title("Fix login bug")
                .status(Status.IN_PROGRESS)
                .priority(Priority.HIGH)
                .createdBy("yashascs")
                .approvalStatus(ApprovalStatus.NOT_REQUIRED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .commentCount(0)
                .attachmentCount(0)
                .build();

        when(ticketService.updateTicket(eq(1L), any(TicketUpdatePatchRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id} - Should return 403 when user is not authorized")
    void updateTicket_whenAccessDenied_shouldReturn403() throws Exception {
        TicketUpdatePatchRequest request = new TicketUpdatePatchRequest();
        request.setStatus(Status.CLOSED);

        when(ticketService.updateTicket(eq(1L), any(TicketUpdatePatchRequest.class)))
                .thenThrow(new AccessDeniedException("You can only update tickets you created or that are assigned to you."));

        mockMvc.perform(patch("/api/tickets/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PATCH /api/tickets/{id} - Should return 404 when ticket does not exist")
    void updateTicket_whenNotFound_shouldReturn404() throws Exception {
        when(ticketService.updateTicket(eq(999L), any(TicketUpdatePatchRequest.class)))
                .thenThrow(new ResourceNotFoundException("Ticket not found with ID: 999"));

        mockMvc.perform(patch("/api/tickets/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new TicketUpdatePatchRequest())))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // POST /api/tickets/{ticketId}/comments (JSON)
    // =========================================================

    @Test
    @DisplayName("POST /api/tickets/{id}/comments - Should return 201 on successful comment")
    void addComment_whenValid_shouldReturn201() throws Exception {
        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Investigating now.");

        CommentResponseDTO response = CommentResponseDTO.builder()
                .id(1L)
                .content("Investigating now.")
                .createdBy("yashascs")
                .createdAt(LocalDateTime.now())
                .attachments(Collections.emptyList())
                .build();

        when(ticketService.addComment(eq(1L), any(CommentRequestDTO.class), isNull()))
                .thenReturn(response);

        mockMvc.perform(post("/api/tickets/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Investigating now."))
                .andExpect(jsonPath("$.createdBy").value("yashascs"));
    }

    @Test
    @DisplayName("POST /api/tickets/{id}/comments - Should return 404 when ticket not found")
    void addComment_whenTicketNotFound_shouldReturn404() throws Exception {
        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("Hello");

        when(ticketService.addComment(eq(999L), any(CommentRequestDTO.class), isNull()))
                .thenThrow(new ResourceNotFoundException("Ticket not found with ID: 999"));

        mockMvc.perform(post("/api/tickets/999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // =========================================================
    // GET /api/tickets/{ticketId}/comments
    // =========================================================

    @Test
    @DisplayName("GET /api/tickets/{id}/comments - Should return 200 with comment list")
    void getComments_shouldReturn200WithList() throws Exception {
        CommentResponseDTO comment = CommentResponseDTO.builder()
                .id(1L)
                .content("Fixed in latest build.")
                .createdBy("anshcs")
                .createdAt(LocalDateTime.now())
                .attachments(Collections.emptyList())
                .build();

        when(ticketService.getComments(eq(1L), anyString(), isNull())).thenReturn(List.of(comment));

        mockMvc.perform(get("/api/tickets/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].content").value("Fixed in latest build."));
    }
}
