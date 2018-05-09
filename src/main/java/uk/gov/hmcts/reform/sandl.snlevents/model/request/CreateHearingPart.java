package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateHearingPart {

    private UUID id;

    private String caseNumber;

    private String caseTitle;

    private String caseType;

    private String hearingType;

    private Duration duration;

    private LocalDate scheduleStart;

    private LocalDate scheduleEnd;
}
