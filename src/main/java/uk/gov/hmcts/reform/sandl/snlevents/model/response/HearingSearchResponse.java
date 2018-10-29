package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;

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
    @JsonIgnore
    private Long listedCount;
    private OffsetDateTime listingDate;

    public Boolean getIsListed() {
        return listedCount > 0;
    }
}