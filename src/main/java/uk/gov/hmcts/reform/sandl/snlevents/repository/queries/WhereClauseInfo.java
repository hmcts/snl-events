package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public class WhereClauseInfo {
    private SessionFilterKey sessionFilterKey;
    private String whereClause;
    private Map<String, Object> keyValuePairs = new HashMap<String, Object>();
}
