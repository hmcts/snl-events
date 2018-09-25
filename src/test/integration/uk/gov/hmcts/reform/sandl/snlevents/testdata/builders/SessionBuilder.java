package uk.gov.hmcts.reform.sandl.snlevents.testdata.builders;

import org.springframework.boot.test.context.TestComponent;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@TestComponent
public class SessionBuilder {
    private UUID id = UUID.randomUUID();
    private OffsetDateTime start = OffsetDateTimeHelper.january2018();
    private Duration duration = Duration.ofMinutes(30);

    public SessionBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public SessionBuilder withStart(OffsetDateTime start) {
        this.start = start;
        return this;
    }

    public SessionBuilder withDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public SessionBuilder withCaseType(String caseType) {
        this.duration = duration;
        return this;
    }

    public Session build() {
        Session session = new Session();
        session.setId(id);
        session.setStart(start);
        session.setDuration(duration);

        return session;
    }

}
