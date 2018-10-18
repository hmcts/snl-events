package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ViewSessionResponse {
    private UUID id;
    private OffsetDateTime start;
    private Long duration;
    private String roomName;
    private String judgeName;
    private String sessionType;

    public ViewSessionResponse(Session session) {
        this.id = session.getId();
        this.start = session.getStart();
        this.duration = session.getDuration().toMinutes();
        this.roomName = session.getRoom() != null ? session.getRoom().getName() : null;
        this.judgeName = session.getPerson() != null ? session.getPerson().getName() : null;
        this.sessionType = session.getSessionType().getDescription();
    }
}
