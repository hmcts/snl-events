package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingPartResponse implements Serializable {
    private UUID id;
    private String caseNumber;
    private String caseTitle;
    private String caseTypeCode;
    private String hearingTypeCode;
    private Duration duration;
    private OffsetDateTime scheduleStart;
    private OffsetDateTime scheduleEnd;
    private Priority priority;
    private UUID reservedJudgeId;
    private String communicationFacilitator;
    private Boolean deleted;
    private UUID sessionId;
    private Long version;

    public HearingPartResponse(HearingPart hearingPart) {
        this.setId(hearingPart.getId());
        this.setCaseNumber(hearingPart.getCaseNumber());
        this.setCaseTitle(hearingPart.getCaseTitle());
        this.setCaseTypeCode(hearingPart.getCaseType().getCode());
        this.setHearingTypeCode(hearingPart.getHearingType().getCode());
        this.setDuration(hearingPart.getDuration());
        this.setScheduleStart(hearingPart.getScheduleStart());
        this.setScheduleEnd(hearingPart.getScheduleEnd());
        this.setPriority(hearingPart.getPriority());
        this.setReservedJudgeId(hearingPart.getReservedJudgeId());
        this.setCommunicationFacilitator(hearingPart.getCommunicationFacilitator());
        this.setDeleted(hearingPart.isDeleted());
        Optional.ofNullable(hearingPart.getSession()).ifPresent(s -> this.setSessionId(s.getId()));
        this.setVersion(hearingPart.getVersion());
    }
}

