package uk.gov.hmcts.reform.sandl.snlevents.model.request.search;

import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class HearingSpecification implements Specification<Hearing> {
    private SearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Hearing> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        ComparisonOperations operation = criteria.getOperation();
        if (operation.equals(ComparisonOperations.EQUALS)) {
            return cb.equal(root.get(criteria.getKey()), criteria.getValue());
        } else if (operation.equals(ComparisonOperations.IN)) {
            return root.get(criteria.getKey()).in(getArrayValues(criteria.getKey()));
        } else if (operation.equals(ComparisonOperations.LIKE)) {
            return cb.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
        } else if (operation.equals(ComparisonOperations.LISTING_STATUS)) {
            // TODO the missing listing subquery
//            Subquery<HearingPart> subquery = query.subquery(HearingPart.class);
//            Root<HearingPart> hpRoot = subquery.from(HearingPart.class);
//            subquery.select(hpRoot.get(HearingPart_.hearingId).get(Hearing_.id)) //TODO: FIX
//                .where(cb.isNotNull(hpRoot.get("sessionId")));
//            return cb.exists(subquery);
            //throw new RuntimeException("Operation not implemented!");
            return cb.isNotNull(root.get("id"));
        } else {
            return cb.isNotNull(root.get("id"));
        }
    }

    //TODO below a quickfix/prototype need to implement for all the fields and cleanup
    private Object getArrayValues(String key) {
        if (criteria.getKey().equals("reservedJudgeId")) {
            List<String> values = ((List<String>)criteria.getValue());
            List<UUID> toReturn = new ArrayList<>();
            for (String value : values) {
                toReturn.add(UUID.fromString(value));
            }
            return toReturn;
        } else if (criteria.getKey().equals("caseType")) {
            List<String> values = ((List<String>)criteria.getValue());
            List<CaseType> toReturn = new ArrayList<>();
            for (String value : values) {
                toReturn.add(new CaseType(value, ""));
            }
            return toReturn;
        } else {
            return criteria.getValue();
        }
    }
}
