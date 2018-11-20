package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingPartResponse implements Serializable {
    private UUID id;

    private UUID sessionId;

    private HearingInfo hearingInfo;

    private Long version;

    private OffsetDateTime start;

    public HearingPartResponse(HearingPart hearingPart) {
        this.id = hearingPart.getId();
        this.sessionId = hearingPart.getSessionId();
        this.version = hearingPart.getVersion();
        this.start = hearingPart.getStart();
        // @TODO lazy loading requires refactor
        this.hearingInfo = new HearingInfo(hearingPart.getHearing(), hearingPart.getSession());
        // in the future because of performance issues
    }
}
