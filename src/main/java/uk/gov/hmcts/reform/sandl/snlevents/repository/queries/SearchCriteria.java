package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.ComparisonOperations;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {
    private String key;
    private ComparisonOperations operation;
    private Object value;
}
