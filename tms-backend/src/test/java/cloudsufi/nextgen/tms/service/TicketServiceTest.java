package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.*;
import cloudsufi.nextgen.tms.entity.*;
import cloudsufi.nextgen.tms.enums.ApprovalStatus;
import cloudsufi.nextgen.tms.enums.FileType;
import cloudsufi.nextgen.tms.enums.Priority;
import cloudsufi.nextgen.tms.enums.Role;
import cloudsufi.nextgen.tms.enums.Status;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.exception.FileProcessingException;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.*;
import cloudsufi.nextgen.tms.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TicketService business logic.
 * Repository and utility dependencies are mocked; the service logic is tested directly.
 *
 * @author Yashas Yadav
 */
@SpringBootTest
class TicketServiceTest {

    @Autowired
    private TicketService ticketService;

    @MockitoBean
    private TicketRepository ticketRepository;

    @MockitoBean
    private TicketHistoryRepository ticketHistoryRepository;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AttachmentRepository attachmentRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private EmailNotificationService emailNotificationService;

    @MockitoBean
    private CommentRepository commentRepository;

    private UserEntity creatorUser;
    private UserEntity assigneeUser;
    private UserEntity mockUser;
    private TicketEntity mockTicket;
    private TicketRaiseRequest validRequest;

    /** Populate SecurityContext so @PreAuthorize("isAuthenticated()") passes. */
    private void setAuthenticatedContext(String email) {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn(email);
        when(auth.isAuthenticated()).thenReturn(true);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @BeforeEach
    void setUp() {
        setAuthenticatedContext("yashas@cs.com");

        creatorUser = UserEntity.builder()
                .id(1L)
                .username("yashascs")
                .email("yashas@cs.com")
                .role(Role.ENGINEERING)
                .build();

        assigneeUser = UserEntity.builder()
                .id(2L)
                .username("anshcs")
                .email("ansh@cs.com")
                .role(Role.IT)
                .build();

        mockUser = creatorUser;

        mockTicket = TicketEntity.builder()
                .id(10L)
                .title("Test Ticket")
                .description("A description")
                .priority(Priority.HIGH)
                .status(Status.OPEN)
                .createdBy(creatorUser)
                .isApprovalRequired(false)
                .approvalStatus(ApprovalStatus.NOT_REQUIRED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        validRequest = new TicketRaiseRequest();
        validRequest.setTitle("Fix login bug");
        validRequest.setDescription("Login fails on Safari");
        validRequest.setPriority(Priority.HIGH);
    }

    // =========================================================
    // getTicketById
    // =========================================================

    @Test
    @DisplayName("Service - getTicketById should return ticket details with username (not email)")
    void getTicketById_whenFound_shouldReturnDetailsWithUsername() {
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(mockTicket));
        when(attachmentRepository.findByTicketIdAndCommentIsNullOrderByUploadedAtAsc(10L))
                .thenReturn(Collections.emptyList());
        when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(10L))
                .thenReturn(Collections.emptyList());

        TicketDetailsResponse response = ticketService.getTicketById(10L);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Test Ticket", response.getTitle());
        assertEquals("yashascs", response.getCreatedBy());
        assertEquals("Unassigned", response.getAssignedTo());
        assertNotEquals("yashas@cs.com", response.getCreatedBy());
    }

    @Test
    void raiseTicket_UserNotFoundInDatabase_ThrowsResourceNotFoundException() {
        when(jwtUtil.extractUser())
                .thenThrow(new ResourceNotFoundException("User not found: ghostuser@cloudsufi.com"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertEquals("User not found: ghostuser@cloudsufi.com", exception.getMessage());
        verifyNoInteractions(ticketRepository, ticketHistoryRepository);
    }
    /**
     * Tests the successful processing and persistence of valid file attachments.
     * Simulates a request containing both an Image and a PDF file. Verifies that
     * the system successfully accepts these MIME types, reads the byte data, and
     * invokes the attachment repository to save each file.
     */
    @Test
    void raiseTicket_WithValidAttachments_SavesAttachmentsSuccessfully() {
        MockMultipartFile imageFile = new MockMultipartFile("attachments", "screenshot.png", "image/png", "fake-image-data".getBytes());
        MockMultipartFile pdfFile = new MockMultipartFile("attachments", "logs.pdf", "application/pdf", "fake-pdf-data".getBytes());

        validRequest.setAttachments(List.of(imageFile, pdfFile));

        when(jwtUtil.extractUser()).thenReturn(mockUser);
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(mockTicket);
        ticketService.raiseTicket(validRequest);

        verify(attachmentRepository, times(2)).save(any(AttachmentEntity.class));
    }

    /**
     * Ensures that the system strictly rejects unsupported file types to maintain security.
     * Simulates an attempt to upload a shell script (executable). Verifies that a
     * {@link BadRequestException} is thrown immediately, and the transaction is aborted
     * before any attachments are persisted to the database.
     */
    @Test
    void raiseTicket_WithUnsupportedFileType_ThrowsBadRequestException() {
        MockMultipartFile textFile = new MockMultipartFile("attachments", "script.sh", "text/x-shellscript", "echo 'hacked'".getBytes());
        validRequest.setAttachments(List.of(textFile));

        when(jwtUtil.extractUser()).thenReturn(mockUser);
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(mockTicket);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertTrue(exception.getMessage().contains("Unsupported file type"));
        verifyNoInteractions(attachmentRepository); // Transaction aborts, no attachments saved
    }

    /**
     * Verifies the system's strict validation against empty or "ghost" files.
     * Simulates a client sending a file parameter with 0 bytes of data (often caused
     * by client-side browser glitches). Ensures that a {@link BadRequestException} is
     * thrown and the transaction is aborted, preventing the saving of empty files.
     */
    @Test
    void raiseTicket_WithEmptyFile_ThrowsBadRequestException() {
        MockMultipartFile emptyFile = new MockMultipartFile("attachments", "empty.png", "image/png", new byte[0]);
        validRequest.setAttachments(List.of(emptyFile));

        when(jwtUtil.extractUser()).thenReturn(mockUser);
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(mockTicket);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertEquals("Attached file 'empty.png' cannot be empty.", exception.getMessage());


        verifyNoInteractions(attachmentRepository);
    }

    /**
     * Simulates a server-side I/O failure while reading the binary stream of an uploaded file.
     * By mocking the {@link MultipartFile} to throw an {@link IOException} when reading bytes,
     * this test verifies that the checked exception is properly caught, wrapped in our custom
     * {@link FileProcessingException}, and the database save operation is aborted.
     *
     * @throws IOException If the mock file stream fails (simulated).
     */
    @Test
    void raiseTicket_IOExceptionDuringFileRead_ThrowsFileProcessingException() throws   IOException {
        MultipartFile corruptedFile = mock(MultipartFile.class);
        when(corruptedFile.isEmpty()).thenReturn(false);
        when(corruptedFile.getContentType()).thenReturn("image/jpeg");
        when(corruptedFile.getOriginalFilename()).thenReturn("corrupted.jpg");
        when(corruptedFile.getBytes()).thenThrow(new IOException("Simulated Stream Error"));

        validRequest.setAttachments(List.of(corruptedFile));
        when(jwtUtil.extractUser()).thenReturn(mockUser);
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(mockTicket);


        FileProcessingException exception = assertThrows(FileProcessingException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertTrue(exception.getMessage().contains("Failed to read attachment data for file"));
        verifyNoInteractions(attachmentRepository);
    }


    /**
     * Tests the successful retrieval of a ticket by a valid ID.
     * Scenario: A valid ticket ID is provided, and the core ticket, along
     * with its associated attachments and history logs, exists in the database.
     * Expected Outcome: The service should successfully fetch data from all
     * three repositories, correctly map the entities (including calculating the BLOB
     * file sizes) into a {@link TicketDetailsResponse}, and return the comprehensive DTO.
     */
    @Test
    @DisplayName("getTicketById - Should return complete ticket details when ID is valid")
    void getTicketById_ValidId_ReturnsComprehensiveResponse() {
        // GIVEN
        Long ticketId = 100L;

        when(ticketRepository.findById(ticketId)).thenReturn(Optional.of(mockTicket));

        AttachmentEntity mockAttachment = AttachmentEntity.builder()
                .id(1L)
                .fileType(FileType.IMAGE)
                .file(new byte[1024])
                .build();
        when(attachmentRepository.findByTicketIdAndCommentIsNullOrderByUploadedAtAsc(ticketId)).thenReturn(List.of(mockAttachment));

        TicketHistoryEntity mockHistory = TicketHistoryEntity.builder()
                .id(1L)
                .description("Ticket Created")
                .createdBy(mockUser)
                .createdAt(LocalDateTime.now())
                .build();
        when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)).thenReturn(List.of(mockHistory));

        TicketDetailsResponse response = ticketService.getTicketById(ticketId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(10L);

        assertThat(response.getAttachments()).hasSize(1);
        assertThat(response.getAttachments().get(0).getFileSizeInBytes()).isEqualTo(1024);

        assertThat(response.getHistory()).hasSize(1);
        assertThat(response.getHistory().get(0).getDescription()).isEqualTo("Ticket Created");

        verify(ticketRepository, times(1)).findById(ticketId);
        verify(attachmentRepository, times(1)).findByTicketIdAndCommentIsNullOrderByUploadedAtAsc(ticketId);
        verify(ticketHistoryRepository, times(1)).findByTicketIdOrderByCreatedAtAsc(ticketId);
    }
    /**
     * Tests the behavior when attempting to retrieve a ticket that does not exist.
     * Scenario: An invalid or non-existent ticket ID is provided to the service.
     * Expected Outcome: The service should throw a {@link ResourceNotFoundException}
     * immediately after failing to find the core ticket.
     */
    @Test
    @DisplayName("getTicketById - Should throw ResourceNotFoundException when ID does not exist")
    void getTicketById_InvalidId_ThrowsResourceNotFoundException() {
        // GIVEN
        Long invalidId = 999L;

        when(ticketRepository.findById(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getTicketById(invalidId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found with ID: " + invalidId);

        verify(ticketRepository, times(1)).findById(invalidId);
    }

    @Test
    @DisplayName("Service - getTicketById should return username for assignee when assigned")
    void getTicketById_whenAssigned_shouldReturnAssigneeUsername() {
        mockTicket.setAssignedTo(assigneeUser);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(mockTicket));
        when(attachmentRepository.findByTicketIdAndCommentIsNullOrderByUploadedAtAsc(10L))
                .thenReturn(Collections.emptyList());
        when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(10L))
                .thenReturn(Collections.emptyList());

        TicketDetailsResponse response = ticketService.getTicketById(10L);

        assertEquals("anshcs", response.getAssignedTo());
        assertNotEquals("ansh@cs.com", response.getAssignedTo());
    }

    @Test
    @DisplayName("Service - getTicketById should throw ResourceNotFoundException when not found")
    void getTicketById_whenNotFound_shouldThrowException() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> ticketService.getTicketById(999L));

        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    @DisplayName("Service - getTicketById history entries should use username not email")
    void getTicketById_historyEntries_shouldUseUsername() {
        TicketHistoryEntity history = TicketHistoryEntity.builder()
                .id(1L)
                .description("Ticket created")
                .ticket(mockTicket)
                .createdBy(creatorUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(mockTicket));
        when(attachmentRepository.findByTicketIdAndCommentIsNullOrderByUploadedAtAsc(10L))
                .thenReturn(Collections.emptyList());
        when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(10L))
                .thenReturn(List.of(history));

        TicketDetailsResponse response = ticketService.getTicketById(10L);

        assertEquals(1, response.getHistory().size());
        assertEquals("yashascs", response.getHistory().get(0).getCreatedBy());
        assertNotEquals("yashas@cs.com", response.getHistory().get(0).getCreatedBy());
    }

    // =========================================================
    // getMyTickets
    // =========================================================

    @Test
    @DisplayName("Service - getMyTickets should return all tickets for IT role")
    void getMyTickets_whenITRole_shouldReturnAllTickets() {
        UserEntity itUser = UserEntity.builder()
                .id(3L).username("admin").email("admin@cs.com").role(Role.IT).build();

        when(jwtUtil.extractUser()).thenReturn(itUser);
        when(ticketRepository.findAllWithAssociations(any(Sort.class)))
                .thenReturn(List.of(mockTicket));
        when(commentRepository.countByTicketId(anyLong())).thenReturn(0L);
        when(attachmentRepository.countByTicketIdAndCommentIsNull(anyLong())).thenReturn(0L);

        List<TicketResponseDTO> result = ticketService.getMyTickets("createdAt", "desc");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findAllWithAssociations(any(Sort.class));
        verify(ticketRepository, never()).findAllByCreatedByOrAssignedTo(any(), any());
    }

    @Test
    @DisplayName("Service - getMyTickets should filter by creator/assignee for non-IT role")
    void getMyTickets_whenNonITRole_shouldFilterByUserOwnership() {
        when(jwtUtil.extractUser()).thenReturn(creatorUser);
        when(ticketRepository.findAllByCreatedByOrAssignedTo(eq(creatorUser), any(Sort.class)))
                .thenReturn(List.of(mockTicket));
        when(commentRepository.countByTicketId(anyLong())).thenReturn(0L);
        when(attachmentRepository.countByTicketIdAndCommentIsNull(anyLong())).thenReturn(0L);

        List<TicketResponseDTO> result = ticketService.getMyTickets("createdAt", "desc");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(ticketRepository).findAllByCreatedByOrAssignedTo(eq(creatorUser), any(Sort.class));
        verify(ticketRepository, never()).findAllWithAssociations(any());
    }

    // =========================================================
    // raiseTicket
    // =========================================================

    @Test
    @DisplayName("Service - raiseTicket should throw BadRequestException when title is blank")
    void raiseTicket_whenTitleBlank_shouldThrowBadRequest() {
        TicketRaiseRequest request = new TicketRaiseRequest();
        request.setTitle("  ");
        request.setDescription("desc");
        request.setPriority(Priority.HIGH);

        when(jwtUtil.extractUser()).thenReturn(creatorUser);

        assertThrows(BadRequestException.class, () -> ticketService.raiseTicket(request));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Service - raiseTicket should throw BadRequestException when description is blank")
    void raiseTicket_whenDescriptionBlank_shouldThrowBadRequest() {
        TicketRaiseRequest request = new TicketRaiseRequest();
        request.setTitle("Fix login bug");
        request.setDescription("");
        request.setPriority(Priority.MEDIUM);

        when(jwtUtil.extractUser()).thenReturn(creatorUser);

        assertThrows(BadRequestException.class, () -> ticketService.raiseTicket(request));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Service - raiseTicket should throw BadRequestException when priority is null")
    void raiseTicket_whenPriorityNull_shouldThrowBadRequest() {
        TicketRaiseRequest request = new TicketRaiseRequest();
        request.setTitle("Fix login bug");
        request.setDescription("Details here");
        request.setPriority(null);

        when(jwtUtil.extractUser()).thenReturn(creatorUser);

        assertThrows(BadRequestException.class, () -> ticketService.raiseTicket(request));
    }

    // =========================================================
    // updateTicket
    // =========================================================

    @Test
    @DisplayName("Service - updateTicket should deny access when user is not creator or assignee")
    void updateTicket_whenNeitherCreatorNorAssignee_shouldThrowAccessDenied() {
        UserEntity otherUser = UserEntity.builder()
                .id(99L).username("otheruser").email("other@cs.com").role(Role.HR).build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(mockTicket));
        when(jwtUtil.extractUser()).thenReturn(otherUser);

        TicketUpdatePatchRequest request = new TicketUpdatePatchRequest();
        request.setStatus(Status.IN_PROGRESS);

        assertThrows(AccessDeniedException.class, () -> ticketService.updateTicket(10L, request));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Service - updateTicket should allow IT role to modify any ticket")
    void updateTicket_whenITRole_shouldAllowModificationOfAnyTicket() {
        UserEntity itUser = UserEntity.builder()
                .id(3L).username("admin").email("admin@cs.com").role(Role.IT).build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(mockTicket));
        when(jwtUtil.extractUser()).thenReturn(itUser);
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(mockTicket);
        when(ticketHistoryRepository.save(any(TicketHistoryEntity.class)))
                .thenReturn(TicketHistoryEntity.builder().build());
        when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(10L))
                .thenReturn(Collections.emptyList());
        when(commentRepository.findByTicketIdOrderByCreatedAtAsc(10L))
                .thenReturn(Collections.emptyList());
        when(commentRepository.countByTicketId(anyLong())).thenReturn(0L);
        when(attachmentRepository.countByTicketIdAndCommentIsNull(anyLong())).thenReturn(0L);

        TicketUpdatePatchRequest request = new TicketUpdatePatchRequest();
        request.setStatus(Status.IN_PROGRESS);

        TicketResponseDTO result = ticketService.updateTicket(10L, request);

        assertNotNull(result);
        verify(ticketRepository).save(any(TicketEntity.class));
    }

    @Test
    @DisplayName("Service - updateTicket should throw ResourceNotFoundException for unknown ticket")
    void updateTicket_whenTicketNotFound_shouldThrowException() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> ticketService.updateTicket(999L, new TicketUpdatePatchRequest()));

        assertTrue(ex.getMessage().contains("999"));
    }

    // =========================================================
    // addComment
    // =========================================================

    @Test
    @DisplayName("Service - addComment should save comment and return DTO")
    void addComment_whenValidRequest_shouldSaveAndReturnComment() {
        CommentEntity savedComment = CommentEntity.builder()
                .id(1L)
                .content("This is a comment")
                .ticket(mockTicket)
                .createdBy(creatorUser)
                .createdAt(LocalDateTime.now())
                .build();

        when(ticketRepository.findById(10L)).thenReturn(Optional.of(mockTicket));
        when(jwtUtil.extractUser()).thenReturn(creatorUser);
        when(commentRepository.save(any(CommentEntity.class))).thenReturn(savedComment);
        when(attachmentRepository.findByCommentIdOrderByUploadedAtAsc(1L))
                .thenReturn(Collections.emptyList());

        CommentRequestDTO request = new CommentRequestDTO();
        request.setContent("This is a comment");

        CommentResponseDTO response = ticketService.addComment(10L, request, null);

        assertNotNull(response);
        assertEquals("This is a comment", response.getContent());
        assertEquals("yashascs", response.getCreatedBy());
        verify(commentRepository).save(any(CommentEntity.class));
    }

    @Test
    @DisplayName("Service - addComment should throw ResourceNotFoundException for unknown ticket")
    void addComment_whenTicketNotFound_shouldThrowException() {
        when(ticketRepository.findById(999L)).thenReturn(Optional.empty());
        when(jwtUtil.extractUser()).thenReturn(creatorUser);

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> ticketService.addComment(999L, new CommentRequestDTO(), null));

        assertTrue(ex.getMessage().contains("999"));
    }
}
