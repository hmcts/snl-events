package uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch;

import lombok.Getter;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;

public enum SearchSessionSelectColumn {
    PERSON_NAME("person_name"),
    PERSON_ID("person_id"),
    ROOM_NAME("room_name"),
    ROOM_ID("room_id"),
    SESSION_TYPE_CODE("session_type_code"),
    SESSION_TYPE_DESCRIPTION("session_type_description"),
    SESSION_ID("session_id"),
    SESSION_START_TIME("session_startTime"),
    SESSION_START_DATE("session_startDate"),
    SESSION_DURATION("session_duration"),
    NO_OF_HEARING_PARTS("no_of_hearing_parts"),
    ALLOCATED_DURATION("allocated_duration"),
    UTILISATION("utilisation"),
    AVAILABLE("available"),
    HAS_MULTISESSIONHEARING_ASSIGNED("has_multisessionhearing_assigned"),
    SESSION_VERSION("session_version");

    @Getter
    private String columnName;

    SearchSessionSelectColumn(String columnName) {
        this.columnName = columnName;
    }

    public static SearchSessionSelectColumn fromString(String columnName) {
        for (SearchSessionSelectColumn sessionSelectColumn : SearchSessionSelectColumn.values()) {
            if (sessionSelectColumn.getColumnName().equalsIgnoreCase(columnName)) {
                return sessionSelectColumn;
            }
        }

        throw new SnlEventsException("Could not find enum value for the given string: " + columnName);
    }
}
