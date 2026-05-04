package cloudsufi.nextgen.tms.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnalyticsResponseDTO {
    private List<LabelCountDTO> byStatus;
    private List<LabelCountDTO> byPriority;
    private List<LabelCountDTO> weeklyCreated;
    private List<LabelCountDTO> byAssignee;
}
