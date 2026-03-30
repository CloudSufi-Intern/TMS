package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.TicketDetailsResponse;
import cloudsufi.nextgen.tms.dto.TicketRaiseRequest;
import cloudsufi.nextgen.tms.dto.TicketRaiseResponse;
import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.TicketHistoryEntity;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.FileType;
import cloudsufi.nextgen.tms.enums.Priority;
import cloudsufi.nextgen.tms.enums.Status;
import cloudsufi.nextgen.tms.exception.AuthenticationException;
import cloudsufi.nextgen.tms.exception.BadRequestException;
import cloudsufi.nextgen.tms.exception.FileProcessingException;
import cloudsufi.nextgen.tms.exception.ResourceNotFoundException;
import cloudsufi.nextgen.tms.repository.AttachmentRepository;
import cloudsufi.nextgen.tms.repository.TicketHistoryRepository;
import cloudsufi.nextgen.tms.repository.TicketRepository;
import cloudsufi.nextgen.tms.repository.UserRepository;
import cloudsufi.nextgen.tms.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for the {@link TicketService}.
 * This class uses JUnit 5 and Mockito to isolate the business logic of tickets.
 * By mocking the database repositories and the Spring Security context, these tests
 * verify validation rules, security enforcement, and proper data mapping without
 * requiring a live database connection.
 *
 * @author Ansh Parnami
 */
@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketService ticketService;

    private TicketRaiseRequest validRequest;
    private UserEntity mockUser;

    @Mock
    private AttachmentRepository attachmentRepository;

    private TicketEntity mockTicket;

    @Mock
    private JwtUtil jwtUtil;

    /**
     * Initializes default, valid mock data before each test execution.
     * This ensures a clean and predictable state across all test cases.
     */
    @BeforeEach
    void setUp() {
        validRequest = TicketRaiseRequest.builder()
                .title("Valid Title")
                .description("Valid detailed description for testing.")
                .priority(Priority.HIGH)
                .attachments(new ArrayList<>())
                .build();

        mockUser = UserEntity.builder()
                .id(1L)
                .email("testuser@cloudsufi.com")
                .build();

        mockTicket = TicketEntity.builder()
                .id(100L)
                .createdBy(mockUser)
                .build();
    }

    /**
     * Cleans up thread-local variables after each test.
     * Crucial for preventing authentication bleed-over between individual tests.
     */
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    /**
     * Tests the scenario where a valid request payload is submitted by an
     * authenticated and existing user. Expects both the ticket and history to be saved.
     */
    @Test
    void raiseTicket_ValidRequest_ReturnsSuccessResponse() {
        when(jwtUtil.extractUser()).thenReturn(mockUser);

        TicketEntity savedTicket = TicketEntity.builder().id(100L).build();
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(savedTicket);

        TicketRaiseResponse response = ticketService.raiseTicket(validRequest);

        assertNotNull(response);
        assertEquals(100L, response.getTicketId());
        assertEquals(Status.OPEN, response.getStatus());
        assertEquals("Ticket raised successfully.", response.getMessage());

        verify(ticketRepository, times(1)).save(any(TicketEntity.class));
        verify(ticketHistoryRepository, times(1)).save(any());
    }


    /**
     * Handles the edge case where an authenticated user's email exists in the JWT token,
     * but the corresponding user record has been deleted or cannot be found in the database.
     */
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
    void raiseTicket_IOExceptionDuringFileRead_ThrowsFileProcessingException() throws IOException {
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
        when(attachmentRepository.findByTicketId(ticketId)).thenReturn(List.of(mockAttachment));

        TicketHistoryEntity mockHistory = TicketHistoryEntity.builder()
                .id(1L)
                .description("Ticket Created")
                .createdBy(mockUser)
                .createdAt(LocalDateTime.now())
                .build();
        when(ticketHistoryRepository.findByTicketId(ticketId)).thenReturn(List.of(mockHistory));

        TicketDetailsResponse response = ticketService.getTicketById(ticketId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(ticketId);

        assertThat(response.getAttachments()).hasSize(1);
        assertThat(response.getAttachments().get(0).getFileSizeInBytes()).isEqualTo(1024);

        assertThat(response.getHistory()).hasSize(1);
        assertThat(response.getHistory().get(0).getDescription()).isEqualTo("Ticket Created");

        verify(ticketRepository, times(1)).findById(ticketId);
        verify(attachmentRepository, times(1)).findByTicketId(ticketId);
        verify(ticketHistoryRepository, times(1)).findByTicketId(ticketId);
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
        verify(attachmentRepository, never()).findByTicketId(anyLong());
        verify(ticketHistoryRepository, never()).findByTicketId(anyLong());
    }
}