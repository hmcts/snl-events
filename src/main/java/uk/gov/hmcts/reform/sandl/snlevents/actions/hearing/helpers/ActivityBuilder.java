package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.helpers;

import uk.gov.hmcts.reform.sandl.snlevents.exceptions.ActivityBuilderException;
import uk.gov.hmcts.reform.sandl.snlevents.model.activities.ActivityStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ActivityBuilder {
    private UUID userTransactionId;
    private List<ActivityLog> activityLogs = new ArrayList<>();

    public static ActivityBuilder activityBuilder() {
        return new ActivityBuilder();
    }

    public ActivityBuilder userTransactionId(UUID userTransactionId) {
        this.userTransactionId = userTransactionId;
        return this;
    }

    public ActivityBuilder withActivity(UUID entityId, String entityName, ActivityStatus activityStatus) {
        this.activityLogs.add(ActivityLog.builder()
            .id(UUID.randomUUID())
            .entityId(entityId)
            .entityName(entityName)
            .status(activityStatus)
            .userTransactionId(this.userTransactionId)
            .build()
        );

        return this;
    }

    public List<ActivityLog> build() {
        validate();
        return this.activityLogs;
    }

    private void validate() {
        if (userTransactionId == null) {
            throw new ActivityBuilderException("User transaction id must be set");
        }

        if (activityLogs.isEmpty()) {
            throw new ActivityBuilderException("No activity provided");
        }
    }
}
