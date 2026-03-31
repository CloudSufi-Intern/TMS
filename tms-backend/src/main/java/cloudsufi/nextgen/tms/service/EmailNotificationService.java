package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.UserEntity;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
/**
 * Service responsible for constructing and dispatching automated email notifications
 * for the Ticket Management System (TMS).
 * <p>
 * This class utilizes Spring's {@link org.springframework.mail.javamail.JavaMailSender}
 * to send rich, MIME-formatted HTML emails. All public notification methods are
 * annotated with {@link org.springframework.scheduling.annotation.Async @Async} to
 * ensure that email processing runs on a separate background thread. This prevents
 * UI latency and ensures that SMTP network calls do not block the main application thread.
 * </p>
 * <p>
 * <b>Key Responsibilities:</b>
 * <ul>
 * <li>Notifying ticket owners and assignees when a ticket's status is updated.</li>
 * <li>Alerting relevant parties when a ticket is reassigned to a new agent.</li>
 * <li>Gracefully catching and logging {@link jakarta.mail.MessagingException} and
 * other network errors to ensure that email delivery failures do not roll back
 * successful database transactions in the calling services.</li>
 * </ul>
 * </p>
 *
 * @author Ansh Parnami
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends an HTML email asynchronously when a ticket's status changes.
     */
    @Async
    public void sendStatusChangeNotification(TicketEntity ticket, String oldStatus, String newStatus) {
        log.info("Sending HTML status change email for Ticket ID: {}", ticket.getId());

        String subject = String.format("Update: Ticket #%d Status Changed to %s", ticket.getId(), newStatus);

        String htmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                    <div style="background-color: #0056b3; color: white; padding: 15px; text-align: center;">
                        <h2 style="margin: 0;">Ticket Status Update</h2>
                    </div>
                    <div style="padding: 20px; color: #333;">
                        <p>Hello,</p>
                        <p>The status of your ticket has been updated.</p>
                        <table style="width: 100%%; border-collapse: collapse; margin-top: 15px;">
                            <tr>
                                <td style="padding: 8px; border-bottom: 1px solid #eee;"><strong>Ticket ID:</strong></td>
                                <td style="padding: 8px; border-bottom: 1px solid #eee;">#%d</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border-bottom: 1px solid #eee;"><strong>Title:</strong></td>
                                <td style="padding: 8px; border-bottom: 1px solid #eee;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border-bottom: 1px solid #eee;"><strong>Old Status:</strong></td>
                                <td style="padding: 8px; border-bottom: 1px solid #eee; color: #888;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 8px; border-bottom: 1px solid #eee;"><strong>New Status:</strong></td>
                                <td style="padding: 8px; border-bottom: 1px solid #eee; color: #28a745; font-weight: bold;">%s</td>
                            </tr>
                        </table>
                        <p style="margin-top: 20px;">Please log in to your dashboard to view more details.</p>
                    </div>
                    <div style="background-color: #f9f9f9; padding: 10px; text-align: center; font-size: 12px; color: #777;">
                        &copy; 2026 CloudSufi NextGen TMS. All rights reserved.
                    </div>
                </div>
                """.formatted(ticket.getId(), ticket.getTitle(), oldStatus, newStatus);

        sendHtmlEmail(ticket.getCreatedBy().getEmail(), subject, htmlBody);

        if (ticket.getAssignedTo() != null) {
            sendHtmlEmail(ticket.getAssignedTo().getEmail(), subject, htmlBody);
        }
    }

    /**
     * Sends HTML emails asynchronously to the creator and new assignee when assignment changes.
     */
    @Async
    public void sendAssigneeChangeNotification(TicketEntity ticket, UserEntity newAssignee) {
        log.info("Sending HTML assignee change email for Ticket ID: {}", ticket.getId());

        String subject = String.format("Ticket #%d Assigned to %s", ticket.getId(), newAssignee.getUsername());

        String creatorHtmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; border: 1px solid #ddd; border-radius: 8px;">
                    <div style="padding: 20px; color: #333;">
                        <h3 style="color: #0056b3;">Ticket Assignment Update</h3>
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Your ticket <strong>#%d - %s</strong> has been assigned to <strong>%s</strong>.</p>
                        <p>They will begin working on it shortly.</p>
                    </div>
                </div>
                """.formatted(ticket.getCreatedBy().getUsername(), ticket.getId(), ticket.getTitle(), newAssignee.getUsername());

        sendHtmlEmail(ticket.getCreatedBy().getEmail(), subject, creatorHtmlBody);

        String assigneeHtmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; border: 1px solid #ddd; border-radius: 8px;">
                    <div style="padding: 20px; color: #333;">
                        <h3 style="color: #28a745;">New Ticket Assigned</h3>
                        <p>Hello <strong>%s</strong>,</p>
                        <p>You have been assigned a new ticket:</p>
                        <div style="background-color: #f1f1f1; padding: 15px; border-left: 4px solid #28a745; margin: 15px 0;">
                            <strong>Ticket ID:</strong> #%d<br>
                            <strong>Title:</strong> %s<br>
                            <strong>Priority:</strong> %s
                        </div>
                        <p>Please log in to the TMS to review the details and begin work.</p>
                    </div>
                </div>
                """.formatted(newAssignee.getUsername(), ticket.getId(), ticket.getTitle(), ticket.getPriority().name());

        sendHtmlEmail(newAssignee.getEmail(), subject, assigneeHtmlBody);
    }

    /**
     * Core helper method to construct and send the MIME message.
     */
    private void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);

            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.debug("HTML Email successfully sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to construct MIME email to: {}. Reason: {}", to, e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}. Reason: {}", to, e.getMessage());
        }
    }
}