package uk.gov.hmcts.reform.sandl.snlevents.repository.specifications;

import org.springframework.data.jpa.domain.Specification;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart_;

import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

public class HearingSpecifications {
    private HearingSpecifications() {
    }

    public static Specification<Hearing> isListed(boolean isListed) {
        return (root, query, cb) -> {
            Subquery<HearingPart> subquery = query.subquery(HearingPart.class);
            Root<HearingPart> hpRoot = subquery.from(HearingPart.class);
            subquery.select(hpRoot);
            if (isListed) {
                subquery.where(cb.equal(hpRoot.get(HearingPart_.hearingId), root),
                    cb.isNotNull(hpRoot.get(HearingPart_.sessionId)));
            } else {
                subquery.where(cb.equal(hpRoot.get(HearingPart_.hearingId), root),
                    cb.isNull(hpRoot.get(HearingPart_.sessionId)));
            }
            return cb.exists(subquery);
        };
    }
}
