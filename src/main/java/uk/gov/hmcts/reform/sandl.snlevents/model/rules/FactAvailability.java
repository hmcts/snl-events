package uk.gov.hmcts.reform.sandl.snlevents.model.rules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FactAvailability implements Serializable {
    private String id;
    private String judgeId;
    private OffsetDateTime start;
    private Duration duration;
}
