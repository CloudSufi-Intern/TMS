package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.TicketRaiseRequest;
import cloudsufi.nextgen.tms.dto.TicketRaiseResponse;
import cloudsufi.nextgen.tms.entity.*;
import cloudsufi.nextgen.tms.enums.ApprovalStatus;
import cloudsufi.nextgen.tms.enums.FileType;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service class responsible for handling the core business logic related to Tickets.
 * This class orchestrates the validation, creation, and history tracking of tickets.
 * It strictly relies on the Spring Security Context to identify the acting user,
 * ensuring secure operations without relying on client-provided user IDs.
 *
 * @author Ansh Parnami
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final JwtUtil jwtUtil;
    private final AttachmentRepository attachmentRepository;

    /**
     * Main entry point to raise a new support ticket in the system.
     * This method is transactional. If saving the ticket or the ticket history fails,
     * the entire operation will roll back to prevent orphaned records in the database.
     *
     * @param request The data transfer object containing the user's ticket input.
     * @return A response object containing the newly created ticket's ID and status.
     * @throws BadRequestException       If the incoming request payload fails business validation.
     * @throws AuthenticationException   If the user is not authenticated in the security context.
     * @throws ResourceNotFoundException If the authenticated user cannot be found in the database.
     */
    @Transactional
    public TicketRaiseResponse raiseTicket(TicketRaiseRequest request) {
        log.info("Initiating ticket creation process for title: '{}'", request.getTitle());

        TicketEntity savedTicket = saveTicketEntity(request);
        saveTicketHistoryEntity(savedTicket);

        List<MultipartFile> attachments = request.getAttachments();
        if (attachments == null) {
            log.warn("Attachments list is NULL. Spring did not bind the files to the DTO.");
        } else {
            log.info("Received request with {} attachments in the DTO.", attachments.size());
        }

        if (attachments != null && !attachments.isEmpty()) {
            saveAttachments(attachments, savedTicket, savedTicket.getCreatedBy());
        }

        log.info("Ticket successfully raised with ID: {}", savedTicket.getId());

        return TicketRaiseResponse.builder()
                .ticketId(savedTicket.getId())
                .status(Status.OPEN)
                .message("Ticket raised successfully.")
                .build();
    }

    /**
     * Extracts the authenticated user's identity, maps the DTO to a JPA Entity,
     * and persists it to the database.
     *
     * @param request The validated request payload.
     * @return The persisted {@link TicketEntity} with its generated ID.
     */
    private TicketEntity saveTicketEntity(TicketRaiseRequest request) {
        log.info("Extracting user identity from Spring Security Context...");
        UserEntity creator=jwtUtil.extractUser();

        log.debug("Building TicketEntity map...");
        TicketEntity ticket = TicketEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority())
                .status(Status.OPEN)
                .sla(null)
                .createdBy(creator)
                .assignedTo(null)
                .isApprovalRequired(false)
                .approvalStatus(ApprovalStatus.NOT_REQUIRED)
                .approver(null)
                .assignedAt(null)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("Saving new TicketEntity to the database...");
        return ticketRepository.save(ticket);
    }

    /**
     * Creates an initial audit log entry in the ticket history table.
     *
     * @param savedTicket The newly created ticket that this history record references.
     */
    private void saveTicketHistoryEntity(TicketEntity savedTicket) {


        TicketHistoryEntity history = TicketHistoryEntity.builder()
                .description("Ticket Created")
                .ticket(savedTicket)
                .createdBy(savedTicket.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .build();

        ticketHistoryRepository.save(history);
        log.info("Ticket history log saved successfully.");
    }

    /**
     * Processes and persists a list of file attachments associated with a support ticket.
     * This method iterates through the provided files, logs their metadata, and enforces
     * strict security validation to ensure only approved file types (Images and PDFs)
     * are uploaded. Valid files are converted into binary arrays and saved to the database.
     * Empty files (0 bytes) are safely ignored and skipped.
     *
     * @param attachments A list of {@link MultipartFile} objects uploaded by the client.
     * @param ticket      The parent {@link TicketEntity} to which these attachments are linked.
     * @param uploader    The {@link UserEntity} representing the person who uploaded the files.
     * @throws BadRequestException     If any file in the payload has an unsupported MIME type
     * (anything other than {@code image/*} or {@code application/pdf}).
     * @throws FileProcessingException If an underlying {@link java.io.IOException} occurs while
     * attempting to read the binary data from the file stream.
     */
    private void saveAttachments(List<MultipartFile> attachments, TicketEntity ticket, UserEntity uploader) {
        log.debug("Commencing attachment processing for Ticket ID: {}", ticket.getId());

        attachments.forEach(file -> {

            log.info("Processing file: '{}', Size: {} bytes, ContentType: {}",
                    file.getOriginalFilename(), file.getSize(), file.getContentType());

            if (file.isEmpty()) {

                log.warn("Skipping file '{}' because it is empty (0 bytes).", file.getOriginalFilename());
                throw new BadRequestException("Attached file '" + file.getOriginalFilename() + "' cannot be empty.");
            }

            String contentType = file.getContentType();
            FileType determinedFileType;

            if (contentType != null && contentType.startsWith("image/")) {
                determinedFileType = FileType.IMAGE;
            } else if ("application/pdf".equalsIgnoreCase(contentType)) {
                determinedFileType = FileType.PDF;
            } else {
                log.warn("Security block: Rejected upload of unsupported file type '{}' for file '{}'",
                        contentType, file.getOriginalFilename());
                throw new BadRequestException("Unsupported file type: " + contentType + ". Only images and PDFs are allowed.");
            }

            try {
                AttachmentEntity attachmentEntity = AttachmentEntity.builder()
                        .file(file.getBytes())
                        .fileType(determinedFileType)
                        .ticket(ticket)
                        .uploadedBy(uploader)
                        .build();

                attachmentRepository.save(attachmentEntity);
                log.info("Successfully saved attachment '{}' to the database.", file.getOriginalFilename());

            } catch (java.io.IOException e) {
                log.error("Failed to read binary data for attachment: {}", file.getOriginalFilename(), e);
                throw new FileProcessingException("Failed to read attachment data for file: " + file.getOriginalFilename());
            }
        });
        log.info("Attachment processing completed.");
    }
}