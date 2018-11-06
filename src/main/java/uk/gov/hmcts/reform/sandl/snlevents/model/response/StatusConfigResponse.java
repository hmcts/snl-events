package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatusConfigResponse implements Serializable {
    private String status;

    private boolean canBeListed;

    private boolean canBeUnlisted;

    public StatusConfigResponse(StatusConfig statusConfig) {
        this.status = statusConfig.getStatus();
        this.canBeListed = statusConfig.isCanBeListed();
        this.canBeUnlisted = statusConfig.isCanBeUnlisted();
    }
}
