package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;

import java.util.UUID;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdjournHearingRequest implements UserTransactional {
    private UUID hearingId;
    private long hearingVersion;
    private UUID userTransactionId;
    @NotBlank
    @Size(max = 500)
    private String description;

    @Override
    public UUID getUserTransactionId() {
        return userTransactionId;
    }
}
