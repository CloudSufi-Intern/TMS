package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.entity.CommentEntity;
import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.TicketHistoryEntity;
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    @Value("${tms.base-url}")
    private String tmsBaseUrl;

    /**
     * Sends an HTML email asynchronously when a ticket's status changes.
     */
    @Async
    public void sendStatusChangeNotification(TicketEntity ticket, String oldStatus, String newStatus, List<TicketHistoryEntity> history, List<CommentEntity> comments) {
        log.info("Sending HTML status change email for Ticket ID: {}", ticket.getId());

        String subject = String.format("Update: Ticket #%d Status Changed to %s", ticket.getId(), newStatus);
        String ticketUrl = tmsBaseUrl + "/tickets/" + ticket.getId();
        String historyHtml = formatHistory(history);
        String commentsHtml = formatComments(comments);

        String htmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                    <div style="background-color: #0056b3; color: white; padding: 15px; text-align: center;">
                        <h2 style="margin: 0;">Ticket Status Update</h2>
                    </div>
                    <div style="padding: 20px; color: #333;">
                        <p>Hello,</p>
                        <p>The status of ticket #%d has been updated.</p>
                        <table style="width: 100%%; border-collapse: collapse; margin-top: 15px; font-size: 14px;">
                            <tr>
                                <td style="padding: 10px; border: 1px solid #eee; background-color: #f9f9f9;"><strong>Title</strong></td>
                                <td style="padding: 10px; border: 1px solid #eee;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border: 1px solid #eee; background-color: #f9f9f9; vertical-align: top;"><strong>Description</strong></td>
                                <td style="padding: 10px; border: 1px solid #eee;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border: 1px solid #eee; background-color: #f9f9f9;"><strong>Old Status</strong></td>
                                <td style="padding: 10px; border: 1px solid #eee; color: #888;">%s</td>
                            </tr>
                            <tr>
                                <td style="padding: 10px; border: 1px solid #eee; background-color: #f9f9f9;"><strong>New Status</strong></td>
                                <td style="padding: 10px; border: 1px solid #eee; color: #28a745; font-weight: bold;">%s</td>
                            </tr>
                        </table>

                        <h4 style="color: #0056b3; margin-top: 25px; margin-bottom: 10px; border-bottom: 2px solid #0056b3; padding-bottom: 5px;">Comments</h4>
                        <div style="border: 1px solid #eee; padding: 15px; border-radius: 5px; max-height: 200px; overflow-y: auto; background-color: #fdfdfd;">
                            %s
                        </div>

                        <h4 style="color: #0056b3; margin-top: 25px; margin-bottom: 10px; border-bottom: 2px solid #0056b3; padding-bottom: 5px;">History</h4>
                        <div style="border: 1px solid #eee; padding: 15px; border-radius: 5px; max-height: 200px; overflow-y: auto; background-color: #fdfdfd;">
                            %s
                        </div>

                        <p style="margin-top: 25px; text-align: center;">
                            <a href="%s" style="background-color: #0056b3; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">View Full Ticket Details</a>
                        </p>
                    </div>
                    <div style="background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777; border-top: 1px solid #ddd;">
                        &copy; 2026 CloudSufi NextGen TMS. All rights reserved.
                    </div>
                </div>
                """.formatted(ticket.getId(), ticket.getTitle(), ticket.getDescription(), oldStatus, newStatus, commentsHtml, historyHtml, ticketUrl);

        sendHtmlEmail(ticket.getCreatedBy().getEmail(), subject, htmlBody);

        if (ticket.getAssignedTo() != null &&
                !Objects.equals(ticket.getAssignedTo().getEmail(), ticket.getCreatedBy().getEmail())) {
            sendHtmlEmail(ticket.getAssignedTo().getEmail(), subject, htmlBody);
        }
    }

    /**
     * Sends HTML emails asynchronously to the creator and new assignee when assignment changes.
     */
    @Async
    public void sendAssigneeChangeNotification(TicketEntity ticket, UserEntity newAssignee, List<TicketHistoryEntity> history, List<CommentEntity> comments) {
        log.info("Sending HTML assignee change email for Ticket ID: {}", ticket.getId());

        String subject = String.format("Ticket #%d Assigned to %s", ticket.getId(), newAssignee.getUsername());
        String ticketUrl = tmsBaseUrl + "/tickets/" + ticket.getId();
        String historyHtml = formatHistory(history);
        String commentsHtml = formatComments(comments);

        // Email for the ticket creator
        String creatorHtmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                    <div style="background-color: #0056b3; color: white; padding: 15px; text-align: center;">
                        <h2 style="margin: 0;">Ticket Assignment Update</h2>
                    </div>
                    <div style="padding: 20px; color: #333;">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>Your ticket <strong>#%d (%s)</strong> has been assigned to <strong>%s</strong>.</p>
                        <p style="margin-top: 25px; text-align: center;">
                            <a href="%s" style="background-color: #0056b3; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">View Ticket</a>
                        </p>
                    </div>
                </div>
                """.formatted(ticket.getCreatedBy().getUsername(), ticket.getId(), ticket.getTitle(), newAssignee.getUsername(), ticketUrl);

        sendHtmlEmail(ticket.getCreatedBy().getEmail(), subject, creatorHtmlBody);

        // Email for the new assignee
        String assigneeHtmlBody = """
                 <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                    <div style="background-color: #28a745; color: white; padding: 15px; text-align: center;">
                        <h2 style="margin: 0;">New Ticket Assigned to You</h2>
                    </div>
                    <div style="padding: 20px; color: #333;">
                        <p>Hello <strong>%s</strong>,</p>
                        <p>You have been assigned a new ticket. Please review the details below.</p>
                        <div style="background-color: #f1f1f1; padding: 15px; border-left: 4px solid #28a745; margin: 15px 0;">
                            <strong>Ticket ID:</strong> #%d<br>
                            <strong>Title:</strong> %s<br>
                            <strong>Description:</strong> %s<br>
                            <strong>Priority:</strong> %s
                        </div>
                        
                        <h4 style="color: #0056b3; margin-top: 25px; margin-bottom: 10px; border-bottom: 2px solid #0056b3; padding-bottom: 5px;">Comments</h4>
                        <div style="border: 1px solid #eee; padding: 15px; border-radius: 5px; max-height: 200px; overflow-y: auto; background-color: #fdfdfd;">
                            %s
                        </div>

                        <h4 style="color: #0056b3; margin-top: 25px; margin-bottom: 10px; border-bottom: 2px solid #0056b3; padding-bottom: 5px;">History</h4>
                        <div style="border: 1px solid #eee; padding: 15px; border-radius: 5px; max-height: 200px; overflow-y: auto; background-color: #fdfdfd;">
                            %s
                        </div>
                        
                        <p style="margin-top: 25px; text-align: center;">
                            <a href="%s" style="background-color: #28a745; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">Open Ticket in TMS</a>
                        </p>
                    </div>
                    <div style="background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777; border-top: 1px solid #ddd;">
                        &copy; 2026 CloudSufi NextGen TMS. All rights reserved.
                    </div>
                </div>
                """.formatted(newAssignee.getUsername(), ticket.getId(), ticket.getTitle(), ticket.getDescription(), ticket.getPriority().name(), commentsHtml, historyHtml, ticketUrl);

        sendHtmlEmail(newAssignee.getEmail(), subject, assigneeHtmlBody);
    }

    /**
     * Notifies all IT users when a new ticket is raised so they can assign it.
     */
    @Async
    public void sendNewTicketNotification(TicketEntity ticket, List<UserEntity> itUsers) {
        if (itUsers == null || itUsers.isEmpty()) return;

        log.info("Sending new ticket notification to {} IT user(s) for Ticket ID: {}", itUsers.size(), ticket.getId());

        String subject = String.format("New Ticket #%d Needs Assignment: %s", ticket.getId(), ticket.getTitle());
        String ticketUrl = tmsBaseUrl + "/tickets/" + ticket.getId();

        String htmlBody = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #ddd; border-radius: 8px; overflow: hidden;">
                    <div style="background-color: #0056b3; color: white; padding: 15px; text-align: center;">
                        <h2 style="margin: 0;">New Ticket Requires Assignment</h2>
                    </div>
                    <div style="padding: 20px; color: #333;">
                        <p>A new support ticket has been raised and is awaiting assignment.</p>
                        <div style="background-color: #f1f1f1; padding: 15px; border-left: 4px solid #0056b3; margin: 15px 0;">
                            <strong>Ticket ID:</strong> #%d<br>
                            <strong>Title:</strong> %s<br>
                            <strong>Description:</strong> %s<br>
                            <strong>Priority:</strong> %s<br>
                            <strong>Raised By:</strong> %s
                        </div>
                        <p style="margin-top: 25px; text-align: center;">
                            <a href="%s" style="background-color: #0056b3; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;">View &amp; Assign Ticket</a>
                        </p>
                    </div>
                    <div style="background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 12px; color: #777; border-top: 1px solid #ddd;">
                        &copy; 2026 CloudSufi NextGen TMS. All rights reserved.
                    </div>
                </div>
                """.formatted(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority().name(),
                ticket.getCreatedBy().getUsername(),
                ticketUrl);

        for (UserEntity itUser : itUsers) {
            sendHtmlEmail(itUser.getEmail(), subject, htmlBody);
        }
    }

    private String formatComments(List<CommentEntity> comments) {
        if (comments == null || comments.isEmpty()) {
            return "<p>No comments yet.</p>";
        }
        return comments.stream()
                .map(comment -> String.format(
                        "<div style='margin-bottom: 12px; padding-bottom: 12px; border-bottom: 1px solid #eee;'>" +
                                "<p style='margin: 0; font-weight: bold; color: #555;'>%s <span style='font-weight: normal; color: #888; font-size: 12px;'>commented at %s</span></p>" +
                                "<p style='margin: 5px 0 0 0;'>%s</p>" +
                                "</div>",
                        comment.getCreatedBy().getUsername(),
                        comment.getCreatedAt().toString(),
                        comment.getContent()
                ))
                .collect(Collectors.joining());
    }

    private String formatHistory(List<TicketHistoryEntity> history) {
        if (history == null || history.isEmpty()) {
            return "<p>No history found.</p>";
        }
        return history.stream()
                .map(entry -> String.format(
                        "<p style='margin: 0 0 8px 0; padding: 0; font-size: 13px; color: #666;'>" +
                                "<strong>%s:</strong> %s by <strong>%s</strong>" +
                                "</p>",
                        entry.getCreatedAt().toString(),
                        entry.getDescription(),
                        entry.getCreatedBy().getUsername()
                ))
                .collect(Collectors.joining());
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
