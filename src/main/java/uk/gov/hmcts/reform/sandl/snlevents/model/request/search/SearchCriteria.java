package uk.gov.hmcts.reform.sandl.snlevents.model.request.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchCriteria {
    private String key;
    private ComparisonOperations operation;
    private Object value;
}
