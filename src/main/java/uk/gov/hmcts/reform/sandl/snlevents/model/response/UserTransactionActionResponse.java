package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UserTransactionAction;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTransactionActionResponse {
    UUID transactionId;
    UserTransactionAction action;
    boolean success;
}
