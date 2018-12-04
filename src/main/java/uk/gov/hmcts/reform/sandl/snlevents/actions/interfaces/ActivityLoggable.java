package uk.gov.hmcts.reform.sandl.snlevents.actions.interfaces;

import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;

import java.util.List;

public interface ActivityLoggable {
    String HEARING_ENTITY = "hearing";

    List<ActivityLog> getActivities();
}
