package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.MinDuration;

import java.time.Duration;
import java.util.UUID;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmendSessionRequest implements UserTransactional {
    @NotNull
    private UUID id;

    @NotNull
    private UUID userTransactionId;

    @MinDuration(minMinutes = 1)
    private Duration durationInSeconds;

    @NotNull
    private String startTime;

    @NotNull
    private String sessionTypeCode;

    @NotNull
    private Long version;
}
