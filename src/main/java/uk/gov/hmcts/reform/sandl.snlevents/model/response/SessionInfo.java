package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;

import java.util.UUID;

@Data
@AllArgsConstructor
public class SessionInfo implements Serializable {

    UUID id;

    OffsetDateTime start;

    Duration duration;

    Person judge;

    Room room;

    public SessionInfo(UUID id, OffsetDateTime start, Duration duration, Room room) {
        this.id = id;
        this.start = start;
        this.duration = duration;
        this.room = room;
    }
}
