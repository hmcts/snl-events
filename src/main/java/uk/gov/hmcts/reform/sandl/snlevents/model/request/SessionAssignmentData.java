package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.Data;

import java.util.UUID;

@Data
public class SessionAssignmentData {
    UUID sessionId;
    long sessionVersion;
}
