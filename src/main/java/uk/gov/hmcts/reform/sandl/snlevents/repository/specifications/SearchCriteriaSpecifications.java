package uk.gov.hmcts.reform.sandl.snlevents.repository.specifications;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

public class SearchCriteriaSpecifications {

    private SearchCriteriaSpecifications() {
    }

    public static Specification<Hearing> equals(String criteriaKey, Object criteriaValue) {
        return (root, query, cb) -> {
            return cb.equal(root.get(criteriaKey), criteriaValue);
        };
    }

    public static Specification<Hearing> in(String criteriaKey, Object values) {
        return (root, query, cb) -> {
            return root.get(criteriaKey).in(values);
        };
    }

    public static Specification<Hearing> like(String criteriaKey, Object criteriaValue) {
        return (root, query, cb) -> {
            // this one will use bind variables therefore it is not allowing sql injection
            // this how the query looks like
            // Hibernate: select hearing0_.id as id1_3_, ....
            // from hearing hearing0_
            // where ( hearing0_.is_deleted=false) and (hearing0_.case_number like ?) limit ?
            return cb.like(root.get(criteriaKey), "%" + criteriaValue.toString() + "%");
        };
    }
}
