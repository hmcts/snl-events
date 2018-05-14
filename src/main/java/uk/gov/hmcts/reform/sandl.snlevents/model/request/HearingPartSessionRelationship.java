package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class HearingPartSessionRelationship {
    UUID sessionId;
    OffsetDateTime start;
}
