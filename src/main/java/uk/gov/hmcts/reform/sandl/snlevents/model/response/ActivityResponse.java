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

    private ActivityStatus hearingActivityStatus;
    private String description;
    private String createdBy;
    private OffsetDateTime createdAt;

    public ActivityResponse(ActivityLog activityLog) {
        this.hearingActivityStatus = activityLog.getStatus();
        this.description = activityLog.getDescription();
        this.createdBy = activityLog.getCreatedBy();
        this.createdAt = activityLog.getCreatedAt();
    }
}
