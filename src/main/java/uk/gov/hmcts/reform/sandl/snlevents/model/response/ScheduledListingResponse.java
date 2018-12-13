package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduledListingResponse {
    private UUID id;
    private OffsetDateTime hearingPartStartTime;
    private OffsetDateTime start;
    private Duration duration;
    private String roomName;
    private String judgeName;
    private String sessionType;
    private UUID hearingPartIdOfCurrentHearing;
    private Long hearingPartVersionOfCurrentHearing;

    public ScheduledListingResponse(Session session, Hearing hearing) {
        this.id = session.getId();
        this.start = session.getStart();

        // A session might contain multiple hearingParts so it's important to get only the one for the current hearing
        HearingPart hearingPartOfCurrentHearing = session.getHearingParts().stream()
            .filter(hp -> hp.getHearing().getId().equals(hearing.getId()))
            .findFirst()
            .orElseThrow(SnlEventsException::new);
        this.hearingPartStartTime = hearingPartOfCurrentHearing.getStart();
        this.hearingPartIdOfCurrentHearing = hearingPartOfCurrentHearing.getId();
        this.hearingPartVersionOfCurrentHearing = hearingPartOfCurrentHearing.getVersion();

        this.duration = hearing.isMultiSession() ? session.getDuration() : hearing.getDuration();

        this.roomName = session.getRoom() != null ? session.getRoom().getName() : null;
        this.judgeName = session.getPerson() != null ? session.getPerson().getName() : null;
        this.sessionType = session.getSessionType() != null ? session.getSessionType().getDescription() : null;
    }
}
