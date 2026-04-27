package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.entity.CommentEntity;
import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.TicketHistoryEntity;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Priority;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;


import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test suite for the {@link EmailNotificationService}.
 * <p>
 * This class isolates the email service logic by mocking the {@link JavaMailSender}.
 * It verifies that the correct number of emails are constructed and dispatched based
 * on the ticket's assignment state, and ensures that SMTP network failures are
 * caught gracefully without crashing the main application thread.
 * </p>
 *
 * @author Ansh Parnami
 */
@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailNotificationService emailNotificationService;

    private TicketEntity mockTicket;
    private UserEntity creator;
    private UserEntity assignee;

    private List<TicketHistoryEntity> history;
    private List<CommentEntity> comments;

    /**
     * Initializes mock entities and environment properties before each test.
     * <p>
     * Uses {@link ReflectionTestUtils} to inject the sender's email address, bypassing
     * the need for a full Spring Application Context to resolve the @Value annotation.
     * </p>
     */
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailNotificationService, "fromEmail", "noreply@cloudsufi.com");

        creator = UserEntity.builder().username("Raiser").email("raiser@company.com").build();
        assignee = UserEntity.builder().username("Agent").email("agent@company.com").build();

        mockTicket = TicketEntity.builder()
                .id(100L)
                .title("Server Down")
                .priority(Priority.HIGH)
                .createdBy(creator)
                .assignedTo(assignee)
                .build();
        history = Collections.emptyList();
        comments = Collections.emptyList();
    }

    /**
     * Tests the status change notification when the ticket has both a creator and an assignee.
     * Verifies that exactly two distinct emails are constructed and sent.
     */
    @Test
    @DisplayName("Status Change - Should send two emails when ticket has an assignee")
    void sendStatusChangeNotification_WithAssignee_SendsTwoEmails() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        emailNotificationService.sendStatusChangeNotification(mockTicket, "OPEN", "IN_PROGRESS",history,comments);

        verify(mailSender, times(2)).createMimeMessage();
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    /**
     * Tests the status change notification when the ticket has a creator but no assignee.
     * Verifies that exactly one email is constructed and sent to avoid NullPointerExceptions.
     */
    @Test
    @DisplayName("Status Change - Should send one email when ticket is unassigned")
    void sendStatusChangeNotification_Unassigned_SendsOneEmail() {
        mockTicket.setAssignedTo(null); // Remove assignee
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        emailNotificationService.sendStatusChangeNotification(mockTicket, "OPEN", "IN_PROGRESS",history,comments);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    /**
     * Tests the assignee change notification.
     * Verifies that both the original ticket creator and the newly assigned agent
     * receive customized notification emails.
     */
    @Test
    @DisplayName("Assignee Change - Should send two distinct emails to creator and new assignee")
    void sendAssigneeChangeNotification_SendsToCreatorAndAssignee() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        emailNotificationService.sendAssigneeChangeNotification(mockTicket, assignee,history,comments);

        verify(mailSender, times(2)).createMimeMessage();
        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }

    /**
     * Tests the fault tolerance of the email service.
     * Simulates an SMTP timeout or connection failure and ensures the exception
     * is caught internally, allowing the main application flow to continue uninterrupted.
     */
    @Test
    @DisplayName("Exception Handling - Should fail silently and log error if mail server is down")
    void sendHtmlEmail_WhenMailSenderThrowsException_CatchesAndFailsSilently() {
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        doThrow(new MailSendException("SMTP timeout")).when(mailSender).send(any(MimeMessage.class));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> {
            emailNotificationService.sendStatusChangeNotification(mockTicket, "OPEN", "IN_PROGRESS",history,comments);
        });

        verify(mailSender, times(2)).send(any(MimeMessage.class));
    }
}