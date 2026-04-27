package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.TicketDetailsResponse;
import cloudsufi.nextgen.tms.dto.TicketRaiseRequest;
import cloudsufi.nextgen.tms.dto.TicketRaiseResponse;
import cloudsufi.nextgen.tms.dto.TicketResponseDTO;
import cloudsufi.nextgen.tms.dto.TicketUpdatePatchRequest;
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
import cloudsufi.nextgen.tms.dto.CommentRequestDTO;
import cloudsufi.nextgen.tms.dto.CommentResponseDTO;
import cloudsufi.nextgen.tms.entity.CommentEntity;
import cloudsufi.nextgen.tms.repository.CommentRepository;

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
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;
    private final CommentRepository commentRepository;


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



    /**
     * Retrieves comprehensive details of a ticket by its unique ID.
     * This includes core ticket information, chronological history logs,
     * and metadata for any associated file attachments.
     *
     * @param ticketId The unique database identifier of the ticket.
     * @return A {@link TicketDetailsResponse} containing all aggregated ticket data.
     * @throws ResourceNotFoundException If no ticket exists with the provided ID.
     */
    public TicketDetailsResponse getTicketById(Long ticketId) {
        log.info("Initiating retrieval of complete ticket details for ID: {}", ticketId);


        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> {
                    log.error("Ticket retrieval failed. ID not found: {}", ticketId);
                    return new ResourceNotFoundException("Ticket not found with ID: " + ticketId);
                });

        List<TicketDetailsResponse.AttachmentMetadata> attachments = fetchAndMapAttachments(ticketId);
        List<TicketDetailsResponse.TicketHistory> historyLogs = fetchAndMapHistory(ticketId);

        return buildTicketResponse(ticket, attachments, historyLogs);
    }

    /**
     * Helper method to fetch and map attachment BLOBs to safe metadata DTOs.
     */
    private List<TicketDetailsResponse.AttachmentMetadata> fetchAndMapAttachments(Long ticketId) {
        return attachmentRepository.findByTicketId(ticketId).stream()
                .map(attr -> TicketDetailsResponse.AttachmentMetadata.builder()
                        .id(attr.getId())
                        .fileType(attr.getFileType() != null ? attr.getFileType().name() : "UNKNOWN")
                        .fileSizeInBytes(attr.getFile() != null ? attr.getFile().length : 0)
                        .build())
                .toList();
    }

    /**
     * Helper method to fetch and map chronological audit logs.
     */
    private List<TicketDetailsResponse.TicketHistory> fetchAndMapHistory(Long ticketId) {
        return ticketHistoryRepository.findByTicketId(ticketId).stream()
                .map(logEntity -> TicketDetailsResponse.TicketHistory.builder()
                        .id(logEntity.getId())
                        .description(logEntity.getDescription())
                        .createdBy(logEntity.getCreatedBy() != null ? logEntity.getCreatedBy().getEmail() : "System")
                        .createdAt(logEntity.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     * Helper method to assemble the final response object.
     */
    private TicketDetailsResponse buildTicketResponse(
            TicketEntity ticket,
            List<TicketDetailsResponse.AttachmentMetadata> attachments,
            List<TicketDetailsResponse.TicketHistory> historyLogs) {

        return TicketDetailsResponse.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .sla(ticket.getSla())
                .createdBy(ticket.getCreatedBy() != null ? ticket.getCreatedBy().getEmail() : "Unknown")
                .assignedTo(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getEmail() : "Unassigned")
                .isApprovalRequired(ticket.isApprovalRequired())
                .approver(ticket.getApprover() != null ? ticket.getApprover().getEmail() : "N/A")
                .approvalStatus(ticket.getApprovalStatus())
                .assignedAt(ticket.getAssignedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .attachments(attachments)
                .history(historyLogs)
                .build();
    }

    /**
     * Returns all tickets where the authenticated user is either the creator (raiser)
     * or the assignee, ordered by creation date descending.
     *
     * This powers the dashboard page, giving the user a unified view of every ticket
     * they raised and every ticket currently assigned to them.
     *
     * The user's identity is extracted from the Spring Security context via
     * {@link JwtUtil#extractUser()} — no request parameters needed.
     *
     * @return List of {@link TicketResponseDTO} for the authenticated user's dashboard.
     * @throws ResourceNotFoundException If the authenticated email cannot be matched to
     *                                   a user record in the database.
     * @author Yashas Yadav
     */
    public List<TicketResponseDTO> getMyTickets() {

        UserEntity user = jwtUtil.extractUser();
        log.info("Fetching dashboard tickets for user: {}", user.getEmail());

        List<TicketEntity> tickets = ticketRepository.findAllByCreatedByOrAssignedTo(user);
        log.info("Found {} ticket(s) for user: {}", tickets.size(), user.getEmail());

        return tickets.stream()
                .map(this::toResponseDTO)
                .toList();
    }

    /**
     * Maps a {@link TicketEntity} to a {@link TicketResponseDTO}.
     * Safely handles nullable associations: assignedTo and approver may be null
     * for newly raised tickets.
     *
     * @param ticket The ticket entity to transform.
     * @return The populated response DTO.
     */
    private TicketResponseDTO toResponseDTO(TicketEntity ticket) {
        return TicketResponseDTO.builder()
                .id(ticket.getId())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .priority(ticket.getPriority())
                .status(ticket.getStatus())
                .sla(ticket.getSla())
                .createdBy(ticket.getCreatedBy().getUsername())
                .assignedTo(ticket.getAssignedTo() != null ? ticket.getAssignedTo().getUsername() : null)
                .isApprovalRequired(ticket.isApprovalRequired())
                .approver(ticket.getApprover() != null ? ticket.getApprover().getUsername() : null)
                .approvalStatus(ticket.getApprovalStatus())
                .assignedAt(ticket.getAssignedAt())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }



    /**
     * Partially updates a ticket's core details (Status, Priority, Assignee).
     * Only fields provided in the request will be updated. Generates an audit log
     * detailing exactly what changed.
     *
     * @param ticketId The ID of the ticket to update.
     * @param request  The DTO containing the fields to update.
     * @return The updated ticket mapped to a Response DTO.
     */
    @Transactional
    public TicketResponseDTO updateTicket(Long ticketId, TicketUpdatePatchRequest request) {
        log.info("Initiating partial update for Ticket ID: {}", ticketId);

        TicketEntity ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

        UserEntity currentUser = jwtUtil.extractUser();
        StringBuilder auditLogBuilder = new StringBuilder("Ticket updated: ");
        boolean isUpdated = false;
        boolean statusChanged = false;
        String oldStatusString = ticket.getStatus().name();
        boolean assigneeChanged = false;

        if (request.getStatus() != null && !request.getStatus().equals(ticket.getStatus())) {
            auditLogBuilder.append(String.format("Status changed from %s to %s. ", ticket.getStatus(), request.getStatus()));
            ticket.setStatus(request.getStatus());
            statusChanged = true;
            isUpdated = true;
        }

        if (request.getPriority() != null && !request.getPriority().equals(ticket.getPriority())) {
            auditLogBuilder.append(String.format("Priority changed from %s to %s. ", ticket.getPriority(), request.getPriority()));
            ticket.setPriority(request.getPriority());
            isUpdated = true;
        }

        if (request.getAssigneeEmail() != null) {
            String currentAssigneeEmail = ticket.getAssignedTo() != null ? ticket.getAssignedTo().getEmail() : "Unassigned";

            if (!request.getAssigneeEmail().equalsIgnoreCase(currentAssigneeEmail)) {
                UserEntity newAssignee = userRepository.findByEmail(request.getAssigneeEmail())
                        .orElseThrow(() -> new ResourceNotFoundException("Assignee email not found: " + request.getAssigneeEmail()));

                ticket.setAssignedTo(newAssignee);
                ticket.setAssignedAt(LocalDateTime.now());
                auditLogBuilder.append(String.format("Assignee changed from %s to %s. ", currentAssigneeEmail, request.getAssigneeEmail()));
                isUpdated = true;
                assigneeChanged = true;
            }
        }

        if (isUpdated) {

            ticket.setUpdatedAt(LocalDateTime.now());
            TicketEntity updatedTicket = ticketRepository.save(ticket);

            TicketHistoryEntity historyLog = TicketHistoryEntity.builder()
                    .description(auditLogBuilder.toString().trim())
                    .ticket(updatedTicket)
                    .createdBy(currentUser)
                    .createdAt(LocalDateTime.now())
                    .build();
            ticketHistoryRepository.save(historyLog);

            List<TicketHistoryEntity> history = ticketHistoryRepository.findByTicketId(ticketId);
            List<CommentEntity> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

            if (statusChanged) {
                emailNotificationService.sendStatusChangeNotification(
                        updatedTicket, oldStatusString, updatedTicket.getStatus().name(), history, comments
                );
            }

            if (assigneeChanged) {
                emailNotificationService.sendAssigneeChangeNotification(
                        updatedTicket, updatedTicket.getAssignedTo(), history, comments
                );
            }

            log.info("Ticket ID: {} successfully updated.", ticketId);
            return toResponseDTO(updatedTicket);
        } else {
            log.info("No changes detected for Ticket ID: {}. Skipping database update.", ticketId);
            return toResponseDTO(ticket);
        }
    }

        /**
         * Adds a new comment to a ticket.
         * User identity is extracted from JWT token.
         *
         * @param ticketId The ID of the ticket to comment on.
         * @param request  The comment content.
         * @return The saved comment as a response DTO.
         * @author Priyanshu Gupta
         */
        public CommentResponseDTO addComment (Long ticketId, CommentRequestDTO request){
            log.info("Adding comment to Ticket ID: {}", ticketId);

            TicketEntity ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with ID: " + ticketId));

            UserEntity currentUser = jwtUtil.extractUser();

            CommentEntity comment = CommentEntity.builder()
                    .content(request.getContent())
                    .ticket(ticket)
                    .createdBy(currentUser)
                    .build();

            CommentEntity savedComment = commentRepository.save(comment);
            log.info("Comment saved successfully for Ticket ID: {}", ticketId);

//            List<TicketHistoryEntity> history = ticketHistoryRepository.findByTicketId(ticketId);
//            List<CommentEntity> comments = commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId);

           // emailNotificationService.sendStatusChangeNotification(ticket, ticket.getStatus().name(), ticket.getStatus().name(), history, comments);

            return toCommentResponseDTO(savedComment);
        }


/**
 * Retrieves all comments for a ticket ordered by creation date ascending.
 *
 * @param  ticketId The ID of the ticket.
 * @return List of comments as response DTOs.
 * @author Priyanshu Gupta
 */
        public List<CommentResponseDTO> getComments(Long ticketId) {
            log.info("Fetching comments for Ticket ID: {}", ticketId);

            if (!ticketRepository.existsById(ticketId)) {
                throw new ResourceNotFoundException("Ticket not found with ID: " + ticketId);
            }

            return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                    .stream()
                    .map(this::toCommentResponseDTO)
                    .toList();
        }

/**
 * Maps a CommentEntity to a CommentResponseDTO.
 */
        private CommentResponseDTO toCommentResponseDTO(CommentEntity comment) {
            return CommentResponseDTO.builder()
                    .id(comment.getId())
                    .content(comment.getContent())
                    .createdBy(comment.getCreatedBy().getUsername())
                    .createdAt(comment.getCreatedAt())
                    .build();
        }
    }
