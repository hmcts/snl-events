package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingInfo implements Serializable {

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
    private boolean deleted;
    private Long version;
    private int numberOfSessions;
    private boolean multiSession;
    private Status status;

    public HearingInfo(Hearing hearing, Session session) {
        this.setId(hearing.getId());
        this.setCaseNumber(hearing.getCaseNumber());
        this.setCaseTitle(hearing.getCaseTitle());
        this.setCaseTypeCode(hearing.getCaseType().getCode());
        this.setHearingTypeCode(hearing.getHearingType().getCode());
        this.setScheduleStart(hearing.getScheduleStart());
        this.setScheduleEnd(hearing.getScheduleEnd());
        this.setPriority(hearing.getPriority());
        this.setReservedJudgeId(hearing.getReservedJudgeId());
        this.setCommunicationFacilitator(hearing.getCommunicationFacilitator());
        this.setDeleted(hearing.isDeleted());
        this.setVersion(hearing.getVersion());
        this.setNumberOfSessions(hearing.getNumberOfSessions());
        this.setMultiSession(hearing.isMultiSession());
        this.setStatus(hearing.getStatus().getStatus());
        this.setDuration((this.isMultiSession() &&  session != null) ? session.getDuration() : hearing.getDuration());
    }

    public HearingInfo(Hearing hearing) {
        this.setId(hearing.getId());
        this.setCaseNumber(hearing.getCaseNumber());
        this.setCaseTitle(hearing.getCaseTitle());
        this.setCaseTypeCode(hearing.getCaseType().getCode());
        this.setHearingTypeCode(hearing.getHearingType().getCode());
        this.setDuration(hearing.getDuration());
        this.setScheduleStart(hearing.getScheduleStart());
        this.setScheduleEnd(hearing.getScheduleEnd());
        this.setPriority(hearing.getPriority());
        this.setReservedJudgeId(hearing.getReservedJudgeId());
        this.setCommunicationFacilitator(hearing.getCommunicationFacilitator());
        this.setDeleted(hearing.isDeleted());
        this.setVersion(hearing.getVersion());
        this.setNumberOfSessions(hearing.getNumberOfSessions());
        this.setMultiSession(hearing.isMultiSession());
        this.setStatus(hearing.getStatus().getStatus());
    }
}
