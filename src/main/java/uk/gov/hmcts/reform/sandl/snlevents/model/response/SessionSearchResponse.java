package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionSearchResponse implements Serializable {
    private UUID sessionId;
    private String personName;
    private String roomName;
    private String sessionTypeDescription;
    private OffsetDateTime startTime;
    private OffsetDateTime startDate;
    private Duration duration;
    private int noOfHearingPartsAssignedToSession;
    private Long allocatedDuration;
    private Long utilisation;
    private Duration available;
}
