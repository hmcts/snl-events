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

import java.util.Comparator;
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
        List<ActivityLog> activityLogs = activityLogRepository.getActivityLogByEntityId(entityId);

        return activityLogs.stream()
            .map(ActivityResponse::new)
            .sorted(Comparator.comparing(ActivityResponse::getCreatedAt))
            .collect(Collectors.toList());
    }
}
