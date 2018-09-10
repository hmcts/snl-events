package uk.gov.hmcts.reform.sandl.snlevents.model.rules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("squid:S3437")
public class FactHearingPart implements Serializable {
    private String id;
    private String sessionId;
    private String caseType;
    private String hearingType;
    private Duration duration;
    private OffsetDateTime scheduleStart;
    private OffsetDateTime scheduleEnd;
    private OffsetDateTime createdAt;
}
