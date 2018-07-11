package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.builder.EqualsBuilder;
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

    Person person;

    Room room;

    String caseType;

    Long version;
}
