package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawHearingRequest implements UserTransactional {
    private UUID hearingId;
    private long hearingVersion;
    private UUID userTransactionId;

    @Override
    public UUID getUserTransactionId() {
        return userTransactionId;
    }
}
