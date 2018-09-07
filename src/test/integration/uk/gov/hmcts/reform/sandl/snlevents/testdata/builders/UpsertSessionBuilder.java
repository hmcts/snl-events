package uk.gov.hmcts.reform.sandl.snlevents.testdata.builders;

import org.springframework.boot.test.context.TestComponent;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UpsertSession;
import uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers.OffsetDateTimeHelper;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@TestComponent
public class UpsertSessionBuilder {
    private UUID id = UUID.randomUUID();
    private UUID transactionId = UUID.randomUUID();
    private OffsetDateTime start = OffsetDateTimeHelper.january2018();
    private Duration duration = Duration.ofMinutes(30);
    private String sessionType = "SCLAIMS";
    private String personId = null;
    private String roomId = null;
    private Long version = 0L;

    public UpsertSessionBuilder fromSession(Session session) {
        id = session.getId();
        start = session.getStart();
        duration = session.getDuration();
        sessionType = session.getSessionType() == null ? null : session.getSessionType().getCode();
        personId = session.getPerson() == null ? null : session.getPerson().getId().toString();
        roomId = session.getRoom() == null ? null : session.getRoom().getId().toString();
        version = session.getVersion();
        return this;
    }

    public UpsertSessionBuilder withId(UUID id) {
        this.id = id;
        return this;
    }

    public UpsertSessionBuilder withTransactionId(UUID id) {
        this.transactionId = id;
        return this;
    }

    public UpsertSessionBuilder withStart(OffsetDateTime start) {
        this.start = start;
        return this;
    }

    public UpsertSessionBuilder withDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    public UpsertSessionBuilder withCaseType(String caseType) {
        this.duration = duration;
        return this;
    }

    public UpsertSession build() {
        UpsertSession us = new UpsertSession();
        us.setUserTransactionId(transactionId);
        us.setId(id);
        us.setDuration(duration);
        us.setSessionType(sessionType);
        us.setStart(start);

        return us;
    }

}
