package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class HearingSessionRelationship {
    UUID userTransactionId;
    UUID hearingId;
    long hearingVersion;
    List<SessionAssignmentData> sessionsData;
    OffsetDateTime start;
}


