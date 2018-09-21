package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.MinDuration;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateHearingPart implements UserTransactional {

    @NotNull
    private UUID id;

    @NotBlank
    @Size(max = 200)
    private String caseNumber;

    @NotBlank
    @Size(max = 200)
    private String caseTitle;

    @NotBlank
    @Size(max = 100)
    private String caseType;

    @Size(max = 100)
    private String hearingType;

    @NotNull
    @MinDuration(minMinutes = 1)
    private Duration duration;

    private OffsetDateTime scheduleStart;

    private OffsetDateTime scheduleEnd;

    private Priority priority;

    private UUID reservedJudgeId;

    private String communicationFacilitator;

    @NotNull
    private UUID userTransactionId;
}
