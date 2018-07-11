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
public class FactAvailability implements Serializable {
    private String id;
    private String judgeId;
    private String roomId;
    private OffsetDateTime start;
    private Duration duration;
}
