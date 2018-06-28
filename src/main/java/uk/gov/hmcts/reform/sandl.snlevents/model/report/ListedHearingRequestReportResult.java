package uk.gov.hmcts.reform.sandl.snlevents.model.report;

import java.time.Duration;
import java.time.OffsetDateTime;

public interface ListedHearingRequestReportResult {
    String getCaseId();

    String getCaseName();

    String getJudge();

    String getHearingType();

    String getCaseType();

    Duration getDuration();

    OffsetDateTime getStartTime();

    String getRoom();
}
