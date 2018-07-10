package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpsertSession implements UserTransactional {
    private UUID id;

    private UUID userTransactionId;

    private OffsetDateTime start;

    private Duration duration;

    private String caseType;

    private String personId;

    private String roomId;

    private Long version;
}
