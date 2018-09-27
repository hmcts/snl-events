package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingPartResponse {
    private UUID id;
    private String caseNumber;
    private String caseTitle;
    private String caseType; // TODO change to case type Code
    private String communicationFacilitator;
    private OffsetDateTime createdAt;
    private Boolean deleted;
    private Duration duration;
    private String hearingType; // TODO change it to hearing Type Code
    private Priority priority;
    private UUID reservedJudgeId;
    private OffsetDateTime scheduleStart;
    private OffsetDateTime scheduleEnd;
    private UUID session; // TODO change to sessionId
    private Long version;

    public HearingPartResponse(HearingPart hearingPart) {
        this.setId(hearingPart.getId());
        this.setCaseNumber(hearingPart.getCaseNumber());
        this.setCaseTitle(hearingPart.getCaseTitle());
        this.setCaseType(hearingPart.getCaseType());
        this.setCommunicationFacilitator(hearingPart.getCommunicationFacilitator());
        this.setCreatedAt(hearingPart.getCreatedAt());
        this.setDeleted(hearingPart.isDeleted());
        this.setDuration(hearingPart.getDuration());
        this.setHearingType(hearingPart.getHearingType().getCode());
        this.setPriority(hearingPart.getPriority());
        this.setReservedJudgeId(hearingPart.getReservedJudgeId());
        this.setScheduleStart(hearingPart.getScheduleStart());
        this.setScheduleEnd(hearingPart.getScheduleEnd());
        Optional.ofNullable(hearingPart.getSession()).ifPresent(s -> this.setSession(s.getId()));
        this.setVersion(hearingPart.getVersion());
    }
}

