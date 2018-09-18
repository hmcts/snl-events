package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.MinDuration;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateListingRequest implements UserTransactional {

    private UUID id;

    @NotEmpty
    @Size(max = 200)
    private String caseNumber;

    @NotEmpty
    @Size(max = 200)
    private String caseTitle;

    @NotEmpty
    @Size(max = 100)
    private String caseType;

    @Size(max = 100)
    private String hearingType;

    @NotNull
    @MinDuration(minMinutes = 1)
    private Duration duration;

    private OffsetDateTime scheduleStart;

    private OffsetDateTime scheduleEnd;

    private OffsetDateTime createdAt;

    private Priority priority;

    private UUID reservedJudgeId;

    private String communicationFacilitator;

    private UUID userTransactionId;

    private Long version;
}


