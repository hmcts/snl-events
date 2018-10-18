package uk.gov.hmcts.reform.sandl.snlevents.model.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;

public class HearingSpecificationBuilder {

    private EntityManager entityManager;

    private final List<SearchCriteria> searchCriteriaList;

    public HearingSpecificationBuilder(EntityManager entityManager) {
        searchCriteriaList = new ArrayList<>();
        this.entityManager = entityManager;
    }

    public HearingSpecificationBuilder of(List<SearchCriteria> searchCriteria) {
        searchCriteriaList.addAll(searchCriteria);
        return this;
    }

    public HearingSpecificationBuilder with(String key, ComparisonOperations operation, Object value) {
        searchCriteriaList.add(new SearchCriteria(key, operation, value));
        return this;
    }

    public Specification<Hearing> build() {
        if (searchCriteriaList.size() == 0) {
            return null;
        }

        List<Specification<Hearing>> specs = new ArrayList<>();
        for (SearchCriteria param : searchCriteriaList) {
            specs.add(new HearingSpecification(param));
        }

        Specification<Hearing> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            result = Specifications.where(result).and(specs.get(i));
        }

        return result;
    }
}
