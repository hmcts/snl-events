package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingWithSessionsResponse {
    private UUID id;
    private String caseNumber;
    private String caseTitle;
    private String caseType;
    private String hearingType;
    private Duration duration;
    private OffsetDateTime scheduleStart;
    private OffsetDateTime scheduleEnd;
    private String priority;
    private String communicationFacilitator;
    private String reservedToJudge;
    private List<ViewSessionResponse> sessions;

    public HearingWithSessionsResponse(Hearing hearing) {
        this.id = hearing.getId();
        this.caseNumber = hearing.getCaseNumber();
        this.caseTitle = hearing.getCaseTitle();
        this.caseType = hearing.getCaseType().getDescription();
        this.hearingType = hearing.getHearingType().getDescription();
        this.duration = hearing.getDuration();
        this.scheduleStart = hearing.getScheduleStart();
        this.scheduleEnd = hearing.getScheduleEnd();
        this.priority = hearing.getPriority().toString();
        this.communicationFacilitator = hearing.getCommunicationFacilitator();
        this.reservedToJudge = hearing.getReservedJudge() != null ? hearing.getReservedJudge().getName() : null;
        this.sessions = hearing.getHearingParts()
            .stream()
            .filter(h -> h.getSession() != null)
            .map(h -> new ViewSessionResponse(h.getSession()))
            .collect(Collectors.toList());
    }
}
