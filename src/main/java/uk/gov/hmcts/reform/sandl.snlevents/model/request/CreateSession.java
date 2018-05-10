package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateSession {
    private UUID id;

    private OffsetDateTime start;

    private Duration duration;

    private String caseType;

    private UUID personId;

    private UUID roomId;
}