package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingSearchResponse implements Serializable {
    private UUID id;
    private String caseNumber;
    private String caseTitle;
    private String caseTypeCode;
    private String caseTypeDescription;
    private String hearingTypeCode;
    private String hearingTypeDescription;
    private Duration duration;
    private OffsetDateTime scheduleStart;
    private OffsetDateTime scheduleEnd;
    private UUID reservedJudgeId;
    private String reservedJudgeName;
    private String communicationFacilitator;
    private Priority priority;
    private Long version;
    private Long listedCount;
    public Boolean getIsListed(){
        return listedCount > 0;
    };
    private OffsetDateTime listingDate;


//
//    public HearingSearchResponse(Hearing hearing) {
//        this.setId(hearing.getId());
//        this.setCaseNumber(hearing.getCaseNumber());
//        this.setCaseTitle(hearing.getCaseTitle());
//        this.setCaseTypeCode(hearing.getCaseType().getCode());
//        this.setHearingTypeCode(hearing.getHearingType().getCode());
//        this.setDuration(hearing.getDuration());
//        this.setScheduleStart(hearing.getScheduleStart());
//        this.setScheduleEnd(hearing.getScheduleEnd());
//        this.setPriority(hearing.getPriority());
//        this.setReservedJudgeId(hearing.getReservedJudgeId());
//        this.setReservedJudgeName("will be name here");//TODO
//        this.setCommunicationFacilitator(hearing.getCommunicationFacilitator());
//        this.setDeleted(hearing.isDeleted());
//        this.setVersion(hearing.getVersion());
//        this.setIsListed(true); //TODO
//        this.setListingDate(OffsetDateTime.now()); //TODO
//    }


}
