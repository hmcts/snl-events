package uk.gov.hmcts.reform.sandl.snlevents.testdata.builders;

import org.springframework.boot.test.context.TestComponent;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
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
    private String caseTypeCode = "SCLAIMS";
    private String caseTypeDesc = "case-type-desc";
    private String caseNumber = "case number";
    private String caseTitle = "case title";
    private String hearingTypeCode = "hearing-type-code";
    private String hearingTypeDesc = "hearing-type-code";
    private Long version;

    public HearingType hearingType = new HearingType(hearingTypeCode, hearingTypeDesc);
    public CaseType caseType = new CaseType(caseTypeCode, caseTypeDesc);

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

    public HearingPartBuilder withVersion(Long version) {
        this.version = version;
        return this;
    }

    public HearingPart build() {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(id);
        hearingPart.setCreatedAt(createdAt);
        hearingPart.setVersion(version);

        return hearingPart;
    }
}
