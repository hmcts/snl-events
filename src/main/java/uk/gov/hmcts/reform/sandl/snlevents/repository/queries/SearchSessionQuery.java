package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import lombok.val;
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
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/*
* SELECT
  id,
  start,
  duration,
  allocated,
  ROUND(allocated / duration * 100, 2) AS utilisation,
  -- case when utilisation > 100 then 0 else duration - allocated
  CASE WHEN ROUND(allocated / duration * 100, 2) > 100 THEN 0 ELSE duration - allocated END AS available
FROM (
       SELECT
         main_session.id,
         main_session.start,
         main_session.duration                             AS duration,
         (SELECT SUM(CASE WHEN h.is_multisession = TRUE
                     THEN s.duration
                     ELSE h.duration END)
          FROM hearing_part
            INNER JOIN session s ON hearing_part.session_id = s.id
            INNER JOIN hearing h ON hearing_part.hearing_id = h.id
          WHERE hearing_part.session_id = main_session.id) AS allocated
       FROM session main_session
     ) AS MAIN_SELECT;
* */


/*
*        SELECT
         main_session.id,
         main_session.start,
         main_session.duration                             AS duration,
         (SELECT SUM(CASE WHEN h.is_multisession = TRUE
                     THEN s.duration
                     ELSE h.duration END)
          FROM hearing_part
            INNER JOIN session s ON hearing_part.session_id = s.id
            INNER JOIN hearing h ON hearing_part.hearing_id = h.id
          WHERE hearing_part.session_id = main_session.id) AS allocated
       FROM session main_session
* */

@Component
public class SearchSessionQuery {

    @Autowired
    HearingRepository hearingRepository;

    @Autowired
    private EntityManager entityManager;

    public List<SessionSearchResponse> search() {

        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<SessionSearchResponse> criteriaQuery = criteriaBuilder.createQuery(SessionSearchResponse.class);
        Root<Session> sessionRoot = criteriaQuery.from(Session.class);

        // count allocated
        Subquery sub  = criteriaQuery.subquery(Duration.class);
        Root subRoot = sub.from(HearingPart.class);
        Join<HearingPart, Session> subSessions = subRoot.join(HearingPart_.session);
        Join<HearingPart, Hearing> subHearing = subRoot.join(HearingPart_.hearing);

//        criteriaBuilder.treat(Session_.duration, Long.class);
        Expression<Boolean> isMultisession = criteriaBuilder.equal(subHearing.get(Hearing_.isMultiSession), true);
        Expression<Long> getSessionDuration = subSessions.get(Session_.duration).as(Long.class);
        Expression<Long> getHearingDuration = subHearing.get(Hearing_.duration).as(Long.class);

        Expression<Long> selectCase = criteriaBuilder.<Long>selectCase().when(isMultisession, getSessionDuration).otherwise(getHearingDuration);

        Expression<Long> sumAllocatedDuration = criteriaBuilder.<Long>sum(selectCase);

        sub.select(sumAllocatedDuration);
        sub.where(criteriaBuilder.equal(subRoot.get(HearingPart_.sessionId), sessionRoot.get(Session_.id)));

        val select = criteriaQuery.multiselect(Arrays.asList(sessionRoot.get(Session_.id),
                sub.getSelection()));
//        criteriaQuery.select(
//            criteriaBuilder.construct(
//                SessionSearchResponse.class,
//                sessionRoot.get(Session_.id),
//                sub.getSelection()
//            )
//        );

        TypedQuery<SessionSearchResponse> query = entityManager.createQuery(select);

        return query.getResultList();

//        List<Selection<?>> selections = new LinkedList<>();
//        selections.add(sessionRoot.get(Session_.id));
//        selections.add(sub.getSelection());

//        TypedQuery<SessionSearchResponse> query = entityManager.createQuery(criteriaQuery.multiselect(selections));
//
//        return query.getResultList();
    }

}
