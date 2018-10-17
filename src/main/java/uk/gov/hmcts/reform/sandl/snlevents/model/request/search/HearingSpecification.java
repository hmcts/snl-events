package uk.gov.hmcts.reform.sandl.snlevents.model.request.search;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@AllArgsConstructor
public class HearingSpecification implements Specification<Hearing> {
    private SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Hearing> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        ComparisonOperations operation = criteria.getOperation();
        if (operation.equals(ComparisonOperations.EQUALS)) {
            return cb.equal(root.get(criteria.getKey()), criteria.getValue());
        } else if (operation.equals(ComparisonOperations.IN)) {
            return root.get(criteria.getKey()).in(criteria.getValue());
        } else if (operation.equals(ComparisonOperations.LIKE)) {
            return cb.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
        } else {
            return null;
        }
    }
}
