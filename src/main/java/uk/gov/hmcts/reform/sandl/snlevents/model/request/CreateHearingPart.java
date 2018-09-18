package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Required;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateHearingPart implements UserTransactional {

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
    private Duration duration;

    private OffsetDateTime scheduleStart;

    private OffsetDateTime scheduleEnd;

    private Priority priority;

    private UUID reservedJudgeId;

    private String communicationFacilitator;

    private UUID userTransactionId;
}
