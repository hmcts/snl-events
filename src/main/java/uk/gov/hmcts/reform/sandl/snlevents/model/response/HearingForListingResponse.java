package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder()
//NOSONAR
public class HearingForListingResponse implements Serializable {
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
    private Long version;
    private Integer priority;
    private String communicationFacilitator;
    private UUID reservedJudgeId;
    private String reservedJudgeName;
    private Integer numberOfSessions;
    private String status;
    private Boolean isMultisession;
}
