package cloudsufi.nextgen.tms.service;

import cloudsufi.nextgen.tms.dto.AnalyticsResponseDTO;
import cloudsufi.nextgen.tms.dto.LabelCountDTO;
import cloudsufi.nextgen.tms.entity.TicketEntity;
import cloudsufi.nextgen.tms.entity.UserEntity;
import cloudsufi.nextgen.tms.enums.Status;
import cloudsufi.nextgen.tms.repository.TicketRepository;
import cloudsufi.nextgen.tms.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TicketRepository ticketRepository;
    private final JwtUtil jwtUtil;

    public AnalyticsResponseDTO getAnalytics() {
        UserEntity user = jwtUtil.extractUser();
        boolean isIT = user.getRole() != null && "IT".equals(user.getRole().name());
        log.info("Fetching analytics for user: {} (IT={})", user.getEmail(), isIT);

        return AnalyticsResponseDTO.builder()
                .byStatus(isIT ? byStatusGlobal() : byStatusForUser(user))
                .byPriority(isIT ? byPriorityGlobal() : byPriorityForUser(user))
                .weeklyCreated(isIT ? weeklyCreatedGlobal() : weeklyCreatedForUser(user))
                .byAssignee(isIT ? byAssigneeGlobal() : byAssigneeForUser(user))
                .build();
    }

    // ── Global (IT) ──────────────────────────────────────────────────────────

    private List<LabelCountDTO> byStatusGlobal() {
        return Arrays.stream(Status.values())
                .map(s -> new LabelCountDTO(s.name(), ticketRepository.countByStatus(s)))
                .collect(Collectors.toList());
    }

    private List<LabelCountDTO> byPriorityGlobal() {
        return ticketRepository.countGroupByPriority().stream()
                .map(row -> new LabelCountDTO(row[0].toString(), (Long) row[1]))
                .collect(Collectors.toList());
    }

    private List<LabelCountDTO> weeklyCreatedGlobal() {
        List<TicketEntity> recent = ticketRepository.findCreatedSince(eightWeeksAgo());
        return buildWeeklyBuckets(recent);
    }

    private List<LabelCountDTO> byAssigneeGlobal() {
        return ticketRepository.countGroupByAssignee().stream()
                .map(row -> new LabelCountDTO(row[0].toString(), (Long) row[1]))
                .sorted(Comparator.comparingLong(LabelCountDTO::getCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    // ── User-scoped (non-IT) ─────────────────────────────────────────────────

    private List<LabelCountDTO> byStatusForUser(UserEntity user) {
        return Arrays.stream(Status.values())
                .map(s -> new LabelCountDTO(s.name(), ticketRepository.countByStatusForUser(s, user)))
                .collect(Collectors.toList());
    }

    private List<LabelCountDTO> byPriorityForUser(UserEntity user) {
        return ticketRepository.countGroupByPriorityForUser(user).stream()
                .map(row -> new LabelCountDTO(row[0].toString(), (Long) row[1]))
                .collect(Collectors.toList());
    }

    private List<LabelCountDTO> weeklyCreatedForUser(UserEntity user) {
        List<TicketEntity> recent = ticketRepository.findCreatedSinceForUser(eightWeeksAgo(), user);
        return buildWeeklyBuckets(recent);
    }

    private List<LabelCountDTO> byAssigneeForUser(UserEntity user) {
        return ticketRepository.countGroupByAssigneeForUser(user).stream()
                .map(row -> new LabelCountDTO(row[0].toString(), (Long) row[1]))
                .sorted(Comparator.comparingLong(LabelCountDTO::getCount).reversed())
                .limit(10)
                .collect(Collectors.toList());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LocalDateTime eightWeeksAgo() {
        return LocalDateTime.now().minusWeeks(8);
    }

    private List<LabelCountDTO> buildWeeklyBuckets(List<TicketEntity> tickets) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");
        Map<String, Long> weekMap = new LinkedHashMap<>();
        for (int i = 7; i >= 0; i--) {
            String label = LocalDate.now().minusWeeks(i).with(DayOfWeek.MONDAY).format(fmt);
            weekMap.put(label, 0L);
        }
        tickets.forEach(t -> {
            String label = t.getCreatedAt().toLocalDate().with(DayOfWeek.MONDAY).format(fmt);
            weekMap.merge(label, 1L, Long::sum);
        });
        return weekMap.entrySet().stream()
                .map(e -> new LabelCountDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }
}
