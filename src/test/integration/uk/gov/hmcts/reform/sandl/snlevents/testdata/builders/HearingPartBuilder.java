package uk.gov.hmcts.reform.sandl.snlevents.testdata.builders;

import org.springframework.boot.test.context.TestComponent;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@TestComponent
public class HearingPartBuilder {
    private UUID id = UUID.randomUUID();
    private OffsetDateTime start = OffsetDateTimeHelper.january2018();
    private OffsetDateTime createdAt = OffsetDateTimeHelper.january2018();
    private Duration duration = Duration.ofMinutes(30);
    private String caseType = "SCLAIMS";
    private String caseNumber = "case number";
    private String caseTitle = "case title";

    public HearingPartBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public HearingPartBuilder withStart(OffsetDateTime start) {
        this.start = start;
        return this;
    }

    public HearingPartBuilder withDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public HearingPartBuilder withCaseType(String caseType) {
        this.duration = duration;
        return this;
    }

    public HearingPart build() {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(id);
        hearingPart.setCreatedAt(createdAt);
        hearingPart.setDuration(duration);
        hearingPart.setCaseType(caseType);
        hearingPart.setCaseNumber(caseNumber);
        hearingPart.setCaseTitle(caseTitle);

        return hearingPart;
    }

}
