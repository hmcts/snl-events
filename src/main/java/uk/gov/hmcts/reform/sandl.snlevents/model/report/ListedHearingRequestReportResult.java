package uk.gov.hmcts.reform.sandl.snlevents.model.report;

public interface ListedHearingRequestReportResult {
     String getCaseId();
     String getCaseName();
     String getJudgeName();
     String getHearingType();
     String getCaseType();
     String getDuration();
     String getDate();
     String getStartTime();
     String getRoomName();
}
