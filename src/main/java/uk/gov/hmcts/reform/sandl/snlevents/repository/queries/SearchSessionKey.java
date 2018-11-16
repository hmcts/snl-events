package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import lombok.Getter;
import lombok.val;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public enum SearchSessionKey {
    START_DATE("startDate", null),
    END_DATE("endDate", null),
    SESSION_TYPE_CODES("sessionTypeCodes", null),
    ROOM_IDS("roomIds", null),
    PERSON_IDS("personIds", null),
    UNLISTED("unlisted", null),
    PART_LISTED("partListed", null),
    FULLY_LISTED("fullyListed", null),
    OVER_LISTED("overListed", null),
    CUSTOM("custom", null),
    ;

    @Getter
    private String key;

    private Object value;

    @Getter
    private boolean paramNeeded = true;

    SearchSessionKey(String key, Object value) {
        this.key = key;
        this.value = value;
        this.paramNeeded = true;
    }

    public static SearchSessionKey fromString(String key, Object value) {
        for (SearchSessionKey searchSessionKey : SearchSessionKey.values()) {
            if (searchSessionKey.key.equalsIgnoreCase(key)) {
                searchSessionKey.value = value;

                return searchSessionKey;
            }
        }

        return null;
    }

    public Object getValue() {
        switch (this) {
            case START_DATE:
            case END_DATE:
                return OffsetDateTime.parse((String) value);
            case SESSION_TYPE_CODES:
            case UNLISTED:
            case PART_LISTED:
            case FULLY_LISTED:
            case OVER_LISTED:
            case CUSTOM:
                return ((ArrayList<Integer>) value);
            case ROOM_IDS:
            case PERSON_IDS:
                return ((ArrayList<String>) value).stream()
                    .filter(s -> !s.isEmpty())
                    .map(id -> UUID.fromString(id)).collect(Collectors.toList());
        }

        return null;
    }

    public String getColumnName() {
        switch (this) {
            case START_DATE: return SearchSessionSelectColumn.SESSION_START.getColumnName();
            case END_DATE: return SearchSessionSelectColumn.SESSION_START.getColumnName();
            case SESSION_TYPE_CODES: return SearchSessionSelectColumn.SESSION_TYPE_CODE.getColumnName();
            case ROOM_IDS: return SearchSessionSelectColumn.ROOM_ID.getColumnName();
            case PERSON_IDS: return SearchSessionSelectColumn.PERSON_ID.getColumnName();
            case UNLISTED:
            case PART_LISTED:
            case FULLY_LISTED:
            case OVER_LISTED:
            case CUSTOM: return SearchSessionSelectColumn.UTILISATION.getColumnName();
        }

        return null;
    }

    public String getWherePredicate(SearchCriteria sc) {
        switch (this) {
            case START_DATE: return String.format(" AND %s > :%s ", getColumnName(), getKey());
            case END_DATE: return String.format(" AND %s < :%s ", getColumnName(), getKey());
            case SESSION_TYPE_CODES: return String.format(" AND %s IN (:%s) ", getColumnName(), getKey());
            case ROOM_IDS: return createInClause(getColumnName(), sc);
            case PERSON_IDS: return createInClause(getColumnName(), sc);
            case UNLISTED:
                paramNeeded = false;
                return String.format(" OR %s = 0 ", getColumnName());
            case PART_LISTED: return String.format(" OR (%s > 0 AND %s < 100) ", getColumnName(), getColumnName());
            case FULLY_LISTED:
                paramNeeded = false;
                return String.format(" OR %s = 100 ", getColumnName());
            case OVER_LISTED:
                paramNeeded = false;
                return String.format(" OR %s > 100 ", getColumnName());
            case CUSTOM: return String.format(" OR (%s > :%s AND %s < :%s)", getColumnName(), getKey());
        }

        return null;
    }

    private String createInClause(String columnName, SearchCriteria sc) {
        if (sc.getOperation() == ComparisonOperations.IN_OR_NULL) {
            // Somebody may set 'in or null' but passed an empty array
            val filteredValues = ((ArrayList<String>)sc.getValue())
                .stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

            if (filteredValues.isEmpty()) {
                paramNeeded = false;
                return String.format(" AND %s IS NULL ", columnName);
            } else {
                return String.format(" AND %s IN (:%s) OR %s IS NULL ", columnName, sc.getKey(), columnName);
            }
        } else {
            return String.format(" AND %s IN (:%s) ", columnName, sc.getKey());
        }
    }
}
