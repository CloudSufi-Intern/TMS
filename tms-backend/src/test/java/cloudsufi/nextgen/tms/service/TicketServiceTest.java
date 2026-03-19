package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.TicketRaiseRequest;
import cloudsufi.nextgen.tms.dto.TicketRaiseResponse;
import cloudsufi.nextgen.tms.entity.AttachmentEntity;
import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.UserEntity;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
     * Helper utility to simulate an active and authenticated user session in Spring Security.
     *
     * @param email The email address of the mocked logged-in user.
     */
    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(email);

        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * Tests the scenario where a valid request payload is submitted by an
     * authenticated and existing user. Expects both the ticket and history to be saved.
     */
    @Test
    void raiseTicket_ValidRequest_ReturnsSuccessResponse() {
        mockSecurityContext("testuser@cloudsufi.com");
        when(userRepository.findByEmail("testuser@cloudsufi.com")).thenReturn(Optional.of(mockUser));

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
     * Verifies that the service fails fast and rejects requests with an empty or null title.
     */
    @Test
    void raiseTicket_MissingTitle_ThrowsBadRequestException() {
        validRequest.setTitle("");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertEquals("Ticket title cannot be null or empty.", exception.getMessage());
        verifyNoInteractions(ticketRepository, ticketHistoryRepository, userRepository);
    }

    /**
     * Verifies that the service fails fast and rejects requests with an empty or null description.
     */
    @Test
    void raiseTicket_MissingDescription_ThrowsBadRequestException() {
        validRequest.setDescription("   ");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertEquals("Ticket description cannot be null or empty.", exception.getMessage());
        verifyNoInteractions(ticketRepository, ticketHistoryRepository, userRepository);
    }

    /**
     * Verifies that the service fails fast and rejects requests where the priority enum is null.
     */
    @Test
    void raiseTicket_MissingPriority_ThrowsBadRequestException() {
        validRequest.setPriority(null);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertEquals("Ticket priority is required.", exception.getMessage());
        verifyNoInteractions(ticketRepository, ticketHistoryRepository, userRepository);
    }

    /**
     * Ensures that requests bypassing the security filter, resulting in a null or
     * unauthenticated context, are strictly rejected before any database operations occur.
     */
    @Test
    void raiseTicket_UnauthenticatedUser_ThrowsAuthenticationException() {
        SecurityContextHolder.setContext(mock(SecurityContext.class));

        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertEquals("User is not authenticated", exception.getMessage());
        verifyNoInteractions(ticketRepository, ticketHistoryRepository);
    }

    /**
     * Handles the edge case where an authenticated user's email exists in the JWT token,
     * but the corresponding user record has been deleted or cannot be found in the database.
     */
    @Test
    void raiseTicket_UserNotFoundInDatabase_ThrowsResourceNotFoundException() {
        mockSecurityContext("ghostuser@cloudsufi.com");
        when(userRepository.findByEmail("ghostuser@cloudsufi.com")).thenReturn(Optional.empty());

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

        mockSecurityContext("testuser@cloudsufi.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
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

        mockSecurityContext("testuser@cloudsufi.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(mockTicket);

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertTrue(exception.getMessage().contains("Unsupported file type"));
        verifyNoInteractions(attachmentRepository); // Transaction aborts, no attachments saved
    }

    /**
     * Verifies the system's resilience against empty or "ghost" files.
     * Simulates a client sending a file parameter with 0 bytes of data (often caused
     * by client-side browser glitches). Ensures that the main ticket is still created
     * successfully, but the empty attachment is silently ignored and not saved.
     */
    @Test
    void raiseTicket_WithEmptyGhostFile_SkipsAttachmentSilently() {
        MockMultipartFile emptyFile = new MockMultipartFile("attachments", "empty.png", "image/png", new byte[0]);
        validRequest.setAttachments(List.of(emptyFile));

        mockSecurityContext("testuser@cloudsufi.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(mockTicket);

        ticketService.raiseTicket(validRequest);

        verify(ticketRepository, times(1)).save(any(TicketEntity.class));
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

        mockSecurityContext("testuser@cloudsufi.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(mockUser));
        when(ticketRepository.save(any(TicketEntity.class))).thenReturn(mockTicket);


        FileProcessingException exception = assertThrows(FileProcessingException.class, () -> {
            ticketService.raiseTicket(validRequest);
        });

        assertTrue(exception.getMessage().contains("Failed to read attachment data for file"));
        verifyNoInteractions(attachmentRepository);
    }
}