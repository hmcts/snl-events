package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import lombok.Getter;

public enum HearingForListingColumn {
    CASE_NUMBER("case_number"),
    CASE_TITLE("case_title"),
    CASE_TYPE_DESCRIPTION("case_type_description"),
    HEARING_TYPE_DESCRIPTION("hearing_type_description"),
    DURATION("duration"),
    COMMUNICATION_FACILITATOR("communication_facilitator"),
    PRIORITY("priority"),
    RESERVED_JUDGE_NAME("reserved_judge_name"),
    NUMBER_OF_SESSIONS("number_of_sessions"),
    SCHEDULE_START("schedule_start"),
    SCHEDULE_END("schedule_end"),
    ;

    @Getter
    private String columnName;

    HearingForListingColumn(String columnName) {
        this.columnName = columnName;
    }

    public static HearingForListingColumn fromString(String columnName) {
        for (HearingForListingColumn sessionSelectColumn : HearingForListingColumn.values()) {
            if (sessionSelectColumn.getColumnName().equalsIgnoreCase(columnName)) {

                return sessionSelectColumn;
            }
        }

        return null;
    }
}
