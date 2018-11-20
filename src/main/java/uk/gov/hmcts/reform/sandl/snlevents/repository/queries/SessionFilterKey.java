package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import lombok.Getter;
import lombok.val;
import org.assertj.core.util.Maps;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public enum SessionFilterKey {
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

    public static final List<String> UTILISATION_KEYS = Arrays.asList(
        SessionFilterKey.UNLISTED.getKey(),
        SessionFilterKey.PART_LISTED.getKey(),
        SessionFilterKey.FULLY_LISTED.getKey(),
        SessionFilterKey.OVER_LISTED.getKey(),
        SessionFilterKey.CUSTOM.getKey()
    );
    public static final String CASE_NOT_HANDLED_MSG = "Case not handled";

    @Getter
    private String key;

    private SearchCriteria searchCriteria;

    SessionFilterKey(String key, SearchCriteria searchCriteria) {
        this.key = key;
        this.searchCriteria = searchCriteria;
    }

    public static SessionFilterKey fromSearchCriteria(SearchCriteria searchCriteria) {
        for (SessionFilterKey sessionFilterKey : SessionFilterKey.values()) {
            if (sessionFilterKey.key.equalsIgnoreCase(searchCriteria.getKey())) {
                sessionFilterKey.searchCriteria = searchCriteria;

                return sessionFilterKey;
            }
        }

        return null;
    }

    public Object getOriginalValue() {
        return this.searchCriteria.getValue();
    }

    public Object getSqlValue() {
        Object value = this.searchCriteria.getValue();
        switch (this) {
            case START_DATE:
            case END_DATE:
                return OffsetDateTime.parse((String) value);
            case CUSTOM:
                return ((List<Integer>) value);
            case SESSION_TYPE_CODES:
                return value;
            case ROOM_IDS:
            case PERSON_IDS:
                return ((List<String>) value).stream()
                    .filter(s -> !s.isEmpty())
                    .map(UUID::fromString).collect(Collectors.toList());
            case UNLISTED:
            case PART_LISTED:
            case FULLY_LISTED:
            case OVER_LISTED:
                return null;
            default: throw new SnlRuntimeException(CASE_NOT_HANDLED_MSG);
        }
    }

    public String getColumnName() {
        switch (this) {
            case START_DATE: return SearchSessionSelectColumn.SESSION_START_TIME.getColumnName();
            case END_DATE: return SearchSessionSelectColumn.SESSION_START_DATE.getColumnName();
            case SESSION_TYPE_CODES: return SearchSessionSelectColumn.SESSION_TYPE_CODE.getColumnName();
            case ROOM_IDS: return SearchSessionSelectColumn.ROOM_ID.getColumnName();
            case PERSON_IDS: return SearchSessionSelectColumn.PERSON_ID.getColumnName();
            case UNLISTED:
            case PART_LISTED:
            case FULLY_LISTED:
            case OVER_LISTED:
            case CUSTOM:
                return SearchSessionSelectColumn.UTILISATION.getColumnName();
            default: throw new SnlRuntimeException(CASE_NOT_HANDLED_MSG);
        }
    }

    public WhereClauseInfo getWherePredicate() {
        String whereClause = null;
        Map<String, Object> keyValuePairs = Maps.newHashMap(getKey(), getSqlValue());
        switch (this) {
            case START_DATE:
                whereClause = String.format(" AND %s > :%s ", getColumnName(), getKey());
                break;
            case END_DATE:
                whereClause = String.format(" AND %s < :%s ", getColumnName(), getKey());
                break;
            case SESSION_TYPE_CODES:
                whereClause = String.format(" AND %s IN (:%s) ", getColumnName(), getKey());
                break;
            case ROOM_IDS: return createInClause();
            case PERSON_IDS: return createInClause();
            case UNLISTED:
                whereClause = String.format(" OR %s = 0 ", getColumnName());
                keyValuePairs = new HashMap<>();
                break;
            case PART_LISTED:
                whereClause = String.format(" OR (%s > 0 AND %s < 100) ", getColumnName(), getColumnName());
                keyValuePairs = new HashMap<>();
                break;
            case FULLY_LISTED:
                whereClause = String.format(" OR %s = 100 ", getColumnName());
                keyValuePairs = new HashMap<>();
                break;
            case OVER_LISTED:
                whereClause = String.format(" OR %s > 100 ", getColumnName());
                keyValuePairs = new HashMap<>();
                break;
            case CUSTOM:
                List<Integer> customValues = (List<Integer>) getSqlValue();
                String customFromKey = "customFrom";
                Object valueFrom = customValues.get(0);
                String customToKey = "customTo";
                Object valueTo = customValues.get(1);

                whereClause = String.format(" OR (%s > :%s AND %s < :%s)",
                    getColumnName(), customFromKey, getColumnName(), customToKey);
                keyValuePairs = new HashMap<>();
                keyValuePairs.put(customFromKey, valueFrom);
                keyValuePairs.put(customToKey, valueTo);

                break;
            default: throw new SnlRuntimeException(CASE_NOT_HANDLED_MSG);
        }

        return new WhereClauseInfo(this, whereClause, keyValuePairs);
    }

    private WhereClauseInfo createInClause() {
        String whereClause;
        Map<String, Object> keyValuePairs = Maps.newHashMap(getKey(), getSqlValue());

        if (searchCriteria.getOperation() == ComparisonOperations.IN_OR_NULL) {
            val filteredValues = ((List<String>)this.getOriginalValue())
                .stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

            if (filteredValues.isEmpty()) {
                whereClause = String.format(" AND %s IS NULL ", getColumnName());
                keyValuePairs = new HashMap<>();
            } else {
                whereClause = String.format(" AND %s IN (:%s) OR %s IS NULL ",
                    getColumnName(), getKey(), getColumnName());
            }
        } else {
            whereClause = String.format(" AND %s IN (:%s) ", getColumnName(), getKey());
        }

        return new WhereClauseInfo(this, whereClause, keyValuePairs);
    }
}
