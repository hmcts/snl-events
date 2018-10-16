package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.MinDuration;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.NullOrNotBlank;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpsertSession implements UserTransactional {
    @NotNull
    private UUID id;

    @NotNull
    private UUID userTransactionId;

    @NotNull
    private OffsetDateTime start;

    @MinDuration(minMinutes = 1)
    private Duration duration;

    private String sessionType;

    @NullOrNotBlank
    private String personId;

    @NullOrNotBlank
    private String roomId;

    private Long version;

    @NotNull
    @Size(max = 255)
    private String sessionTypeCode;
}
