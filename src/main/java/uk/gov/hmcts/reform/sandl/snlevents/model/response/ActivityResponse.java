package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.activities.ActivityStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ActivityResponse {

    private ActivityStatus activityStatus;
    private String description;
    private String createdBy;
    private OffsetDateTime createdAt;

    public ActivityResponse(ActivityLog activityLog) {
        this.activityStatus = activityLog.getStatus();
        this.description = activityLog.getDescription();
        this.createdBy = prepareUsernameForResponse(activityLog.getCreatedBy());
        this.createdAt = activityLog.getCreatedAt();
    }

    private String prepareUsernameForResponse(String username) {
        return username.split(":")[1]; // usernames in db contains prefix with service wich sent the request e.g.
        // snl-api:username
    }
}
