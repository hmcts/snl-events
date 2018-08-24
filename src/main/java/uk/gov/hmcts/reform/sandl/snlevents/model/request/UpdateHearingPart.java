package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateHearingPart implements UserTransactional {

    private UUID id;

    private String caseNumber;

    private String caseTitle;

    private String caseType;

    private String hearingType;

    private Duration duration;

    private OffsetDateTime scheduleStart;

    private OffsetDateTime scheduleEnd;

    private OffsetDateTime createdAt;

    private Priority priority;

    private UUID reservedJudgeId;

    private String communicationFacilitator;

    private UUID userTransactionId;

    private Long version;
}
