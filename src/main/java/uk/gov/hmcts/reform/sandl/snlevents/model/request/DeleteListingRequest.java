package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteListingRequest implements UserTransactional {

    private UUID hearingPartId;

    private Long hearingPartVersion;

    private UUID userTransactionId;
}
