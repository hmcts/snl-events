package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.VersionInfo;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingWithSessionsResponse {
    private UUID id;
    private String caseNumber;
    private String caseTitle;
    private String caseType;
    private String hearingType;
    private Duration duration;
    private int numberOfSessions;
    private boolean isMultiSession;
    private OffsetDateTime scheduleStart;
    private OffsetDateTime scheduleEnd;
    private String priority;
    private String communicationFacilitator;
    private String reservedToJudge;
    private Status status;
    private List<ViewSessionResponse> sessions;
    private List<VersionInfo> hearingPartsVersions;
    private PossibleActions possibleActions;
    private long version;

    @JsonIgnore
    private StatusConfig statusConfig;

    @JsonIgnore
    private OffsetDateTime listingDate;

    public HearingWithSessionsResponse(Hearing hearing) {
        this.id = hearing.getId();
        this.caseNumber = hearing.getCaseNumber();
        this.caseTitle = hearing.getCaseTitle();
        this.caseType = hearing.getCaseType() != null ? hearing.getCaseType().getDescription() : null;
        this.hearingType = hearing.getHearingType() != null ? hearing.getHearingType().getDescription() : null;
        this.duration = hearing.getDuration();
        this.numberOfSessions = hearing.getNumberOfSessions();
        this.isMultiSession = hearing.isMultiSession();
        this.scheduleStart = hearing.getScheduleStart();
        this.scheduleEnd = hearing.getScheduleEnd();
        this.priority = hearing.getPriority() != null ? hearing.getPriority().toString() : null;
        this.communicationFacilitator = hearing.getCommunicationFacilitator();
        this.reservedToJudge = hearing.getReservedJudge() != null ? hearing.getReservedJudge().getName() : null;
        this.status = hearing.getStatus() != null ? hearing.getStatus().getStatus() : null;
        this.statusConfig = hearing.getStatus();
        this.version = hearing.getVersion() != null ? hearing.getVersion() : null;
        this.sessions = hearing.getHearingParts()
            .stream()
            .filter(hp -> hp.getSession() != null)
            .map(hp -> new ViewSessionResponse(hp.getSession()))
            .sorted(comparing(ViewSessionResponse::getStart))
            .collect(Collectors.toList());
        this.hearingPartsVersions = hearing.getHearingParts().stream().map(hp -> {
            val versionInfo = new VersionInfo();
            versionInfo.setId(hp.getId());
            versionInfo.setVersion(hp.getVersion());

            return versionInfo;
        }).collect(Collectors.toList());
        if (!this.sessions.isEmpty()) {
            this.listingDate = this.sessions.get(0).getStart();
            // thing to use native query instead of JPA Entity
        }
    }
}
