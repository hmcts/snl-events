package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class DragAndDropSessionRequest implements UserTransactional {
    @NotNull
    private UUID sessionId;

    @NotNull
    private OffsetDateTime start;

    @Min(1)
    private Long durationInSeconds;

    private String personId;

    private String roomId;

    @NotNull
    private UUID userTransactionId;

    @NotNull
    private Long version;
}
