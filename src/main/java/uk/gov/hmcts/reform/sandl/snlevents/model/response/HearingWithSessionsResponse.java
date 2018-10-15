package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

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
    private Long duration;
    private OffsetDateTime scheduleStart;
    private OffsetDateTime scheduleEnd;
    private String priority;
    private String communicationFacilitator;
    private String reservedToJudge;
    private List<ViewSessionResponse> sessions;
    //it's only placeholder for fetching notes by snl-notes service
    private Boolean[] notes;

    public HearingWithSessionsResponse(Hearing hearing) {
        this.id = hearing.getId();
        this.caseNumber = hearing.getCaseNumber();
        this.caseTitle = hearing.getCaseTitle();
        this.caseType = hearing.getCaseType().getDescription();
        this.hearingType = hearing.getHearingType().getDescription();
        this.duration = hearing.getDuration().toMinutes();
        this.scheduleStart = hearing.getScheduleStart();
        this.scheduleEnd = hearing.getScheduleEnd();
        this.priority = hearing.getPriority().toString();
        this.communicationFacilitator = hearing.getCommunicationFacilitator();
        this.reservedToJudge = "@todo: to get this we need to change Hearing to have Reserved Judge not just his id";
        this.sessions = hearing.getHearingParts()
            .stream()
            .map(h -> h.getSession() != null ? new ViewSessionResponse(h.getSession()) : null)
            .filter(h -> h != null)
            .collect(Collectors.toList());
        this.notes = new Boolean[0];
    }
}
