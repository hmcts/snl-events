package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;

import java.util.UUID;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteListingRequest implements UserTransactional {
    @NotNull
    private UUID hearingId;

    @NotNull
    private Long hearingVersion;

    @NotNull
    private UUID userTransactionId;
}
