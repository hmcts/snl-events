package uk.gov.hmcts.reform.sandl.snlevents.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.ActivityLog;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.ActivityResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ActivityLogRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("activity-log")
public class ActivityLogController {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @GetMapping(path = "/{entityId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ActivityResponse> getActivitiesByEntityId(@PathVariable("entityId") UUID entityId) {
        List<ActivityLog> activityLogs = activityLogRepository.getActivityLogByEntityIdOrderByCreatedAtAsc(entityId);
        // As there won't be many activityLogs linked with one entity there's no need to limit results. Most of the
        // activities would set entities in their final state, so it won't be possible to do any operation on them.

        return activityLogs.stream()
            .map(ActivityResponse::new)
            .collect(Collectors.toList());
    }
}
