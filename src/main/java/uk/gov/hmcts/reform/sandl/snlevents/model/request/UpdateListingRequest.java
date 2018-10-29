package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.MinDuration;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateListingRequest {

    @NotNull
    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String caseNumber;

    @Size(max = 200)
    private String caseTitle;

    @NotBlank
    @Size(max = 255)
    private String caseTypeCode;

    @Size(max = 255)
    private String hearingTypeCode;

    @NotNull
    @MinDuration(minMinutes = 1)
    private Duration duration;

    private OffsetDateTime scheduleStart;

    private OffsetDateTime scheduleEnd;

    private Priority priority;

    private UUID reservedJudgeId;

    @Size(max = 255)
    private String communicationFacilitator;

    @NotNull
    private UUID userTransactionId;
    @NotNull
    private Long version;
}


