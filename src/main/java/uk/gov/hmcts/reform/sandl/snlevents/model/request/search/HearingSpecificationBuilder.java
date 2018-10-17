package uk.gov.hmcts.reform.sandl.snlevents.model.request.search;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

import java.util.ArrayList;
import java.util.List;

@Component
public class HearingSpecificationBuilder {

    private final List<SearchCriteria> params;

    public HearingSpecificationBuilder() {
        params = new ArrayList<>();
    }

    public HearingSpecificationBuilder of(List<SearchCriteria> searchCriteria) {
        params.addAll(searchCriteria);
        return this;
    }

    public HearingSpecificationBuilder with(String key, ComparisonOperations operation, Object value) {
        params.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public Specification<Hearing> build() {
        if (params.size() == 0) {
            return null;
        }

        List<Specification<Hearing>> specs = new ArrayList<>();
        for (SearchCriteria param : params) {
            specs.add(new HearingSpecification(param));
        }

        Specification<Hearing> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            result = Specifications.where(result).and(specs.get(i));
        }
        return result;
    }
}
