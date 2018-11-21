package uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch.SessionFilterKey;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public class WhereClauseInfo {
    private SessionFilterKey sessionFilterKey;
    private String whereClause;
    private Map<String, Object> keyValuePairs = new HashMap<>();
}
