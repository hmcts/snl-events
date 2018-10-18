package uk.gov.hmcts.reform.sandl.snlevents.repository.specifications;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
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
            specs.add(getSpecification(param));
        }

        Specification<Hearing> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            result = Specifications.where(result).and(specs.get(i));
        }

        return result;
    }

    private Specification<Hearing> getSpecification(SearchCriteria criteria) {
        ComparisonOperations operation = criteria.getOperation();
        if (criteria.getKey().equals("listingStatus")
            && operation.equals(ComparisonOperations.EQUALS)) {
            return HearingSpecifications.isListed(criteria.getValue().toString().equals("listed"));
        } else if (operation.equals(ComparisonOperations.EQUALS)) {
            return SearchCriteriaSpecifications.equals(criteria.getKey(), criteria.getValue());
        } else if (operation.equals(ComparisonOperations.IN)) {
            return SearchCriteriaSpecifications.in(criteria.getKey(), getArrayValues(criteria.getKey(),
                (List<String>) criteria.getValue()));
        } else if (operation.equals(ComparisonOperations.LIKE)) {
            return SearchCriteriaSpecifications.like(criteria.getKey(), criteria.getValue());
        } else {
            throw new IllegalArgumentException("Hearing SearchCriteria keys or values not supported");
        }
    }

    private Object getArrayValues(String criteriaKey, List<String> criteriaValue) {
        if (criteriaKey.equals("reservedJudgeId")) {
            return mapToObjectList(UUID::fromString, criteriaValue);
        } else if (criteriaKey.equals("caseType")) {
            return mapToObjectList(value -> new CaseType(value, ""), criteriaValue);
        } else if (criteriaKey.equals("hearingType")) {
            return mapToObjectList(value -> new HearingType(value, ""), criteriaValue);
        } else if (criteriaKey.equals("priority")) {
            return mapToObjectList(Priority::valueOf, criteriaValue);
        } else if (criteriaKey.equals("communicationFacilitator")) {
            return mapToObjectList(value -> value, criteriaValue);
        } else {
            return criteriaValue;
        }
    }

    private <T> List mapToObjectList(Function<String, T> operation, List<String> criteria) {
        List<T> toReturn = new ArrayList<>();
        for (String value : criteria) {
            toReturn.add(operation.apply(value));
        }
        return toReturn;
    }

}
