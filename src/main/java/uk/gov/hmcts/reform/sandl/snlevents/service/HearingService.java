package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart_;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing_;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.specifications.ComparisonOperations;
import uk.gov.hmcts.reform.sandl.snlevents.repository.specifications.SearchCriteria;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

@Service
public class HearingService {

    @Autowired
    HearingRepository hearingRepository;

    @Autowired
    private EntityManager entityManager;

    public Page<HearingSearchResponse> search(List<SearchCriteria> searchCriteriaList, Pageable pageable) {

//        Specification<Hearing> specification = new HearingSpecificationBuilder(entityManager)
//            .of(searchCriteriaList).build();

//        Specification<Hearing> query = new HearingSearchQueryBuilder(entityManager)
//            .of(searchCriteriaList).build();

//        Metamodel m = entityManager.getMetamodel();
//        EntityType<Hearing> Hearing_ = m.entity(Hearing.class);
//        EntityType<Person> Person_ = m.entity(Person.class);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<HearingSearchResponse> cq = cb.createQuery(HearingSearchResponse.class);
        Root<Hearing> hearingRoot = cq.from(Hearing.class); //root entity


        //Subquery
        Subquery<String> sqPerson = cq.subquery(String.class);
        Root<Person> sqPersonNU = sqPerson.from(Person.class);
        sqPerson.where(
            cb.equal(sqPersonNU.get("id"), hearingRoot)  //join subquery with main query
        );
        sqPerson.select(cb.greatest(sqPersonNU.<String>get("name")));


        //Subquery
        Subquery<Long> sqSent = cq.subquery(Long.class);
        Root<HearingPart> sqSentNU = sqSent.from(HearingPart.class);
        sqSent.select(cb.count(sqSentNU));
        sqSent.where(
            cb.equal(sqSentNU.get(HearingPart_.hearingId), hearingRoot),  //join subquery with main query
            cb.isNotNull(sqSentNU.get(HearingPart_.sessionId))
        );

        //Subquery
        Subquery<OffsetDateTime> sqListing = cq.subquery(OffsetDateTime.class);
        Root<HearingPart> sqListingNU = sqListing.from(HearingPart.class);
        sqListing.where(
            cb.equal(sqListingNU.get(HearingPart_.hearingId), hearingRoot),  //join subquery with main query
            cb.isNotNull(sqListingNU.get(HearingPart_.sessionId))
        );
        sqListing.select(cb.least(sqListingNU.<OffsetDateTime>get("start")));


        Predicate restrictions = cb.conjunction();

        for (SearchCriteria criteria: searchCriteriaList             ) {
            ComparisonOperations operation = criteria.getOperation();
            Predicate pred = null;
            if (criteria.getKey().equals("listingStatus")
                && operation.equals(ComparisonOperations.EQUALS)) {
                boolean isListed = criteria.getValue().toString().equals("listed");

                Subquery<HearingPart> subquery = cq.subquery(HearingPart.class);
                Root<HearingPart> hpRoot = subquery.from(HearingPart.class);
                subquery.select(hpRoot);
                if (isListed) {
                    subquery.where(cb.equal(hpRoot.get(HearingPart_.hearingId), hearingRoot),
                        cb.isNotNull(hpRoot.get(HearingPart_.sessionId)));
                } else {
                    subquery.where(cb.equal(hpRoot.get(HearingPart_.hearingId), hearingRoot),
                        cb.isNull(hpRoot.get(HearingPart_.sessionId)));
                }
                pred = cb.exists(subquery);

            } else if (operation.equals(ComparisonOperations.EQUALS)) {
                pred =  cb.equal(hearingRoot.get(criteria.getKey()), criteria.getValue());
            } else if (operation.equals(ComparisonOperations.IN)) {
                pred = hearingRoot.get(criteria.getKey()).in(getArrayValues(criteria.getKey(),
                    (List<String>) criteria.getValue()));
            } else if (operation.equals(ComparisonOperations.IN_OR_NULL)) {
                pred = cb.or(
                    hearingRoot.get(criteria.getKey()).in(getArrayValues(criteria.getKey(),
                        (List<String>) criteria.getValue())),
                    hearingRoot.get(criteria.getKey()).isNull());

            } else if (operation.equals(ComparisonOperations.LIKE)) {
                pred = cb.like(hearingRoot.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            } else {
                throw new IllegalArgumentException("Hearing SearchCriteria keys or values not supported");
            }
            restrictions = cb.and(restrictions, pred);
        }

        cq.where(restrictions);
        cq.orderBy(cb.asc(hearingRoot.get("caseNumber")), cb.asc(hearingRoot.get("createdAt")));

        List<Selection<?>> selections = new LinkedList<>();
        selections.add(hearingRoot.get(Hearing_.id));
        selections.add(hearingRoot.get("caseNumber"));
        selections.add(hearingRoot.get("caseTitle"));
        selections.add(hearingRoot.get("caseType").get("code"));
        selections.add(hearingRoot.get("caseType").get("description"));
        selections.add(hearingRoot.get("hearingType").get("code"));
        selections.add(hearingRoot.get("hearingType").get("description"));
        selections.add(hearingRoot.get("duration"));
        selections.add(hearingRoot.get("scheduleStart"));
        selections.add(hearingRoot.get("scheduleEnd"));
        selections.add(hearingRoot.get("reservedJudge").get("id"));
        selections.add(sqPerson.getSelection());
        selections.add(hearingRoot.get("communicationFacilitator"));
        selections.add(hearingRoot.get("priority"));
        selections.add(hearingRoot.get("version"));
        selections.add(sqSent.getSelection());
        selections.add(sqListing.getSelection());

        // one page of results as per pageable
        TypedQuery<HearingSearchResponse> q = entityManager.createQuery(cq.multiselect(selections));
        q.setFirstResult(pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        // total possible results count for paging only
        CriteriaQuery<Long> cqLongCount = cb.createQuery(Long.class);
        cqLongCount.where(restrictions);
        Root<Hearing> hearingRootCount = cqLongCount.from(Hearing.class);
        CriteriaQuery<Long> select = cqLongCount.select(cb.count(hearingRootCount));
        TypedQuery<Long> cqCount = entityManager.createQuery(select);
        Long qCount = cqCount.getSingleResult();

        // run it
        return new PageImpl<>(q.getResultList(), pageable, qCount);
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
