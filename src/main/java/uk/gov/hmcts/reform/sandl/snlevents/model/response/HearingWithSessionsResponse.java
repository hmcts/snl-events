package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
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
    private Integer numberOfSessions;
    private Boolean isMultiSession;
    private OffsetDateTime scheduleStart;
    private OffsetDateTime scheduleEnd;
    private String priority;
    private String communicationFacilitator;
    private String reservedToJudge;
    private List<ViewSessionResponse> sessions;
    private List<VersionInfo> hearingPartsVersions;

    public HearingWithSessionsResponse(Hearing hearing) {
        this.id = hearing.getId();
        this.caseNumber = hearing.getCaseNumber();
        this.caseTitle = hearing.getCaseTitle();
        this.caseType = hearing.getCaseType().getDescription();
        this.hearingType = hearing.getHearingType().getDescription();
        this.duration = hearing.getDuration();
        this.numberOfSessions = hearing.getNumberOfSessions();
        this.isMultiSession = hearing.getIsMultiSession();
        this.scheduleStart = hearing.getScheduleStart();
        this.scheduleEnd = hearing.getScheduleEnd();
        this.priority = hearing.getPriority().toString();
        this.communicationFacilitator = hearing.getCommunicationFacilitator();
        this.reservedToJudge = hearing.getReservedJudge() != null ? hearing.getReservedJudge().getName() : null;
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
    }
}
