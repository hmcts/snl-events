package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.model.Priority;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.CaseType_;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart_;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType_;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing_;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person_;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session_;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponseForAmendment;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.Subquery;

@Component
public class HearingQueries {

    @Autowired
    HearingRepository hearingRepository;

    @Autowired
    private EntityManager entityManager;

    public HearingSearchResponseForAmendment get(UUID id) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HearingSearchResponseForAmendment> criteriaQuery = criteriaBuilder
            .createQuery(HearingSearchResponseForAmendment.class);
        Root<Hearing> hearingRoot = criteriaQuery.from(Hearing.class);

        Subquery<String> subQueryPerson = createPersonNameSelect(criteriaBuilder, criteriaQuery, hearingRoot);
        Subquery<String> subQueryJudgeAssigned = createJudgeAssignedSelect(criteriaBuilder, criteriaQuery, hearingRoot);
        Subquery<Long> subQueryListingCount = createListingCountSelect(criteriaBuilder, criteriaQuery, hearingRoot);
        Subquery<OffsetDateTime> subQueryListingStart = createListingStartSelect(criteriaBuilder,
            criteriaQuery, hearingRoot);

        List<Selection<?>> selections = createSelections(hearingRoot, subQueryPerson,
            subQueryListingCount, subQueryListingStart, subQueryJudgeAssigned);

        SearchCriteria sc = new SearchCriteria("id", ComparisonOperations.EQUALS, id);
        Predicate restrictions = createWherePredicates(Arrays.asList(sc), criteriaBuilder, criteriaQuery, hearingRoot);
        criteriaQuery.where(restrictions);

        return entityManager.createQuery(criteriaQuery.multiselect(selections)).getSingleResult();
    }

    public Page<HearingSearchResponse> search(List<SearchCriteria> searchCriteriaList, Pageable pageable) {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<HearingSearchResponse> criteriaQuery = criteriaBuilder.createQuery(HearingSearchResponse.class);
        Root<Hearing> hearingRoot = criteriaQuery.from(Hearing.class);

        Subquery<String> subQueryPerson = createPersonNameSelect(criteriaBuilder, criteriaQuery, hearingRoot);
        Subquery<Long> subQueryListingCount = createListingCountSelect(criteriaBuilder, criteriaQuery, hearingRoot);
        Subquery<OffsetDateTime> subQueryListingStart = createListingStartSelect(criteriaBuilder,
            criteriaQuery, hearingRoot);

        List<Selection<?>> selections = createSelections(hearingRoot, subQueryPerson,
            subQueryListingCount, subQueryListingStart);

        Predicate restrictions = createWherePredicates(searchCriteriaList, criteriaBuilder, criteriaQuery, hearingRoot);
        criteriaQuery.where(restrictions);
        criteriaQuery.orderBy(
            criteriaBuilder.asc(hearingRoot.get(Hearing_.caseNumber)),
            criteriaBuilder.asc(hearingRoot.get(Hearing_.createdAt)));

        TypedQuery<HearingSearchResponse> pageableQuery = createPageableQuery(pageable, criteriaQuery, selections);
        TypedQuery<Long> cqCount = createCountQueryForPaging(criteriaBuilder, restrictions);

        Long queryResultsCount = cqCount.getSingleResult();
        List<HearingSearchResponse> queryResults = pageableQuery.getResultList();

        return new PageImpl<>(queryResults, pageable, queryResultsCount);
    }

    private List<Selection<?>> createSelections(Root<Hearing> hearingRoot,
                                                Subquery<String> subQueryPerson,
                                                Subquery<Long> subQueryListingCount,
                                                Subquery<OffsetDateTime> subQueryListingStart,
                                                Subquery<String> subQueryJudgeAssigned) {
        List<Selection<?>> selections = this.createSelections(hearingRoot, subQueryPerson,
            subQueryListingCount, subQueryListingStart);

        selections.add(subQueryJudgeAssigned.getSelection());

        return selections;
    }

    private List<Selection<?>> createSelections(Root<Hearing> hearingRoot,
                                                Subquery<String> subQueryPerson,
                                                Subquery<Long> subQueryListingCount,
                                                Subquery<OffsetDateTime> subQueryListingStart) {
        List<Selection<?>> selections = new LinkedList<>();

        selections.add(hearingRoot.get(Hearing_.id));
        selections.add(hearingRoot.get(Hearing_.caseNumber));
        selections.add(hearingRoot.get(Hearing_.caseTitle));
        selections.add(hearingRoot.get(Hearing_.caseType).get(CaseType_.code));
        selections.add(hearingRoot.get(Hearing_.caseType).get(CaseType_.description));
        selections.add(hearingRoot.get(Hearing_.hearingType).get(HearingType_.code));
        selections.add(hearingRoot.get(Hearing_.hearingType).get(HearingType_.description));
        selections.add(hearingRoot.get(Hearing_.duration));
        selections.add(hearingRoot.get(Hearing_.scheduleStart));
        selections.add(hearingRoot.get(Hearing_.scheduleEnd));
        selections.add(hearingRoot.get(Hearing_.reservedJudge).get(Person_.id));
        selections.add(subQueryPerson.getSelection());
        selections.add(hearingRoot.get(Hearing_.communicationFacilitator));
        selections.add(hearingRoot.get(Hearing_.priority));
        selections.add(hearingRoot.get(Hearing_.version));
        selections.add(subQueryListingCount.getSelection());
        selections.add(subQueryListingStart.getSelection());

        return selections;
    }

    private TypedQuery<HearingSearchResponse> createPageableQuery(Pageable pageable,
                                                                  CriteriaQuery<HearingSearchResponse> cq,
                                                                  List<Selection<?>> selections) {
        TypedQuery<HearingSearchResponse> q = entityManager.createQuery(cq.multiselect(selections));

        q.setFirstResult(pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        return q;
    }

    private TypedQuery<Long> createCountQueryForPaging(CriteriaBuilder cb, Predicate restrictions) {
        CriteriaQuery<Long> cqLongCount = cb.createQuery(Long.class);

        cqLongCount.where(restrictions);
        Root<Hearing> hearingRootCount = cqLongCount.from(Hearing.class);
        CriteriaQuery<Long> select = cqLongCount.select(cb.count(hearingRootCount));

        return entityManager.createQuery(select);
    }

    private Predicate createWherePredicates(List<SearchCriteria> searchCriteriaList,
                                            CriteriaBuilder cb,
                                            CriteriaQuery<?> cq,
                                            Root<Hearing> hearingRoot) {
        Predicate restrictions = cb.conjunction();

        for (SearchCriteria criteria: searchCriteriaList) {
            ComparisonOperations operation = criteria.getOperation();
            Predicate restriction = null;

            if (criteria.getKey().equals("listingStatus")
                && operation.equals(ComparisonOperations.EQUALS)) {

                boolean isListed = criteria.getValue().toString().equals("listed");
                restriction = createListingStatusPredicate(cb, cq, hearingRoot, isListed);
            } else if (operation.equals(ComparisonOperations.EQUALS)) {
                restriction = createEqualsPredicate(cb, hearingRoot, criteria);
            } else if (operation.equals(ComparisonOperations.IN)) {
                restriction = createInPredicate(hearingRoot, criteria);
            } else if (operation.equals(ComparisonOperations.IN_OR_NULL)) {
                restriction = createInOrNullPredicate(cb, hearingRoot, criteria);
            } else if (operation.equals(ComparisonOperations.LIKE)) {
                restriction = createLikePredicate(cb, hearingRoot, criteria);
            } else {
                throw new IllegalArgumentException("Hearing SearchCriteria keys or values not supported");
            }
            restrictions = cb.and(restrictions, restriction);
        }

        return restrictions;
    }

    private Predicate createLikePredicate(CriteriaBuilder cb, Root<Hearing> hearingRoot, SearchCriteria criteria) {
        return cb.like(hearingRoot.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
    }

    private Predicate createInOrNullPredicate(CriteriaBuilder cb, Root<Hearing> hearingRoot, SearchCriteria criteria) {
        Path<Object> rootKey = getRootKeyFromFieldName(hearingRoot, criteria.getKey());

        return cb.or(
            rootKey.in(getArrayValues(criteria.getKey(), (List<String>) criteria.getValue())),
            rootKey.isNull());
    }

    private Path<Object> getRootKeyFromFieldName(Root<Hearing> hearingRoot, String key) {
        Path<Object> rootKey;

        if (key.contains(".")) {
            rootKey = hearingRoot.get(key.split("\\.") [0]).get(key.split("\\.") [1]);
        } else {
            rootKey = hearingRoot.get(key);
        }

        return rootKey;
    }

    private Predicate createInPredicate(Root<Hearing> hearingRoot, SearchCriteria criteria) {
        Path<Object> rootKey = getRootKeyFromFieldName(hearingRoot, criteria.getKey());

        return rootKey.in(getArrayValues(criteria.getKey(), (List<String>) criteria.getValue()));
    }

    private Predicate createEqualsPredicate(CriteriaBuilder cb, Root<Hearing> hearingRoot, SearchCriteria criteria) {
        return cb.equal(hearingRoot.get(criteria.getKey()), criteria.getValue());
    }

    private Predicate createListingStatusPredicate(CriteriaBuilder cb,
                                                   CriteriaQuery<?> cq,
                                                   Root<Hearing> hearingRoot,
                                                   boolean isListed) {
        Subquery<HearingPart> subQuery = cq.subquery(HearingPart.class);
        Root<HearingPart> hpRoot = subQuery.from(HearingPart.class);
        subQuery.select(hpRoot);
        if (isListed) {
            subQuery.where(cb.equal(hpRoot.get(HearingPart_.hearingId), hearingRoot),
                cb.isNotNull(hpRoot.get(HearingPart_.sessionId)));
        } else {
            subQuery.where(cb.equal(hpRoot.get(HearingPart_.hearingId), hearingRoot),
                cb.isNull(hpRoot.get(HearingPart_.sessionId)));
        }
        return  cb.exists(subQuery);
    }

    private Subquery<OffsetDateTime> createListingStartSelect(CriteriaBuilder cb,
                                                                 CriteriaQuery<?> cq,
                                                                 Root<Hearing> hearingRoot) {
        Subquery<OffsetDateTime> subQueryListingStart = cq.subquery(OffsetDateTime.class);

        Root<HearingPart> subQueryListingStartRoot = subQueryListingStart.from(HearingPart.class);
        Join<HearingPart, Session> join = subQueryListingStartRoot.join(HearingPart_.session, JoinType.INNER);
        subQueryListingStart.where(
            //join subquery with main query
            cb.equal(subQueryListingStartRoot.get(HearingPart_.hearingId), hearingRoot),
            // additional filters
            cb.isNotNull(subQueryListingStartRoot.get(HearingPart_.sessionId))
        );
        subQueryListingStart.select(cb.least(join.<OffsetDateTime>get(Session_.start)));

        return subQueryListingStart;
    }

    private Subquery<String> createJudgeAssignedSelect(CriteriaBuilder cb,
                                                                 CriteriaQuery<?> cq,
                                                                 Root<Hearing> hearingRoot) {
        Subquery<String> judgeAssignedSelect = cq.subquery(String.class);

        Root<HearingPart> subQueryListingStartRoot = judgeAssignedSelect.from(HearingPart.class);
        Join<HearingPart, Session> join = subQueryListingStartRoot.join(HearingPart_.session, JoinType.INNER);
        Join<Session, Person> personJoin = join.join(Session_.person, JoinType.INNER);
        judgeAssignedSelect.where(
            //join subquery with main query
            cb.equal(subQueryListingStartRoot.get(HearingPart_.hearingId), hearingRoot),
            // additional filters
            cb.isNotNull(subQueryListingStartRoot.get(HearingPart_.sessionId))
        );
        judgeAssignedSelect.select(cb.least(personJoin.<String>get(Person_.name)));

        return judgeAssignedSelect;
    }

    private Subquery<Long> createListingCountSelect(CriteriaBuilder cb,
                                                    CriteriaQuery<?> cq,
                                                    Root<Hearing> hearingRoot) {
        Subquery<Long> subQueryListingCount = cq.subquery(Long.class);

        Root<HearingPart> subQueryListingCountRoot = subQueryListingCount.from(HearingPart.class);
        subQueryListingCount.select(cb.count(subQueryListingCountRoot));
        subQueryListingCount.where(
            //join subquery with main query
            cb.equal(subQueryListingCountRoot.get(HearingPart_.hearingId), hearingRoot),
            // additional filters
            cb.isNotNull(subQueryListingCountRoot.get(HearingPart_.sessionId))
        );

        return subQueryListingCount;
    }

    private Subquery<String> createPersonNameSelect(CriteriaBuilder cb,
                                                    CriteriaQuery<?> cq,
                                                    Root<Hearing> hearingRoot) {
        Subquery<String> subQueryPerson = cq.subquery(String.class);

        Root<Person> subQueryPersonRoot = subQueryPerson.from(Person.class);
        subQueryPerson.where(
            //join subquery with main query
            cb.equal(subQueryPersonRoot.get("id"), hearingRoot)
        );
        subQueryPerson.select(cb.greatest(subQueryPersonRoot.<String>get(Person_.name)));

        return subQueryPerson;
    }

    private Object getArrayValues(String criteriaKey, List<String> criteriaValue) {
        if (criteriaKey.equals("reservedJudge.id")) {
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
