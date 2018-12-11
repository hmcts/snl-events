package uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;

import java.util.List;

@SuppressWarnings("squid:S1214")
public interface ActivityLoggable {
    List<ActivityLog> getActivities();
}
