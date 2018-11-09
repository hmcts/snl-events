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
import javax.persistence.Query;
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
import java.util.stream.Collectors;

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

class SqlCriteriaMap {
    public SearchCriteria searchCriteria;

}

@Component
public class SearchSessionQuery {

    String SEARCH_SESSION_QUERY = "SELECT\n"
        + "  *,\n"
        + "  CASE WHEN allocated_duration > 100\n"
        + "    THEN 0\n"
        + "  ELSE session_duration - allocated_duration END AS available\n"
        + "FROM (\n"
        + "       SELECT\n"
        + "         *,\n"
        + "         ROUND(allocated_duration / session_duration * 100, 2) AS utilisation\n"
        + "       FROM (\n"
        + "              SELECT\n"
        + "                p.id                                              AS person_id,\n"
        + "                p.name                                            AS person_name,\n"
        + "                r.id                                              AS room_id,\n"
        + "                r.name                                            AS room_name,\n"
        + "                st.code                                           AS session_type_code,\n"
        + "                st.description                                    AS session_type_decription,\n"
        + "                main_session.id                                   AS session_id,\n"
        + "                main_session.start                                AS session_start,\n"
        + "                main_session.duration                             AS session_duration,\n"
        + "                (SELECT count(id)\n"
        + "                 FROM hearing_part\n"
        + "                 WHERE session_id = main_session.id)              AS no_of_hearing_parts,\n"
        + "                (SELECT SUM(CASE WHEN h.is_multisession = TRUE\n"
        + "                  THEN s.duration\n"
        + "                            ELSE h.duration END)\n"
        + "                 FROM hearing_part\n"
        + "                   INNER JOIN session s ON hearing_part.session_id = s.id\n"
        + "                   INNER JOIN hearing h ON hearing_part.hearing_id = h.id\n"
        + "                 WHERE hearing_part.session_id = main_session.id) AS allocated_duration\n"
        + "              FROM session main_session\n"
        + "                LEFT JOIN person p ON main_session.person_id = p.id\n"
        + "                LEFT JOIN room r ON main_session.room_id = r.id\n"
        + "                LEFT JOIN session_type st ON main_session.session_type_code = st.code\n"
        + "            ) AS MAIN_SELECT\n"
        + "     ) AS OUTTER\n"
        + "WHERE 1 = 1\n"
        + "      <WHERE_CLAUSES>\n";
//        + "      -- start date\n"
//        + "      AND session_start > '2018-11-06 00:00:00' :: date\n"
//        + "      -- end date\n"
//        + "      AND session_start < '2018-11-07 00:00:00' :: date\n"
//        + "      AND session_type_code IN ('small-claims')\n"
//        + "      AND person_name IN ('Judge Linda') OR person_name IS NULL\n"
//        + "      AND utilisation > 0\n"
//        + "ORDER BY session_start ASC\n"
//        + "LIMIT :itemsPerPage OFFSET :pageNumber;";

    @Autowired
    private EntityManager entityManager;

    List<String> innerKeys = Arrays.asList("startDate", "endDate", "sessionType", "roomId", "personId");
//    List<String> outerKeys = Arrays.asList("listingDetails", "listingDetailsCustomFrom", "listingDetailsCustomTo");

    public List<SessionSearchResponse> search(List<SearchCriteria> searchCriteriaList) {
        val startTimeCriteria = new SearchCriteria();
        startTimeCriteria.setKey("startDate");
        startTimeCriteria.setOperation(ComparisonOperations.EQUALS);
        startTimeCriteria.setValue("2018-11-06 00:00:00");

        val personCriteria = new SearchCriteria();
        personCriteria.setKey("personId");
        personCriteria.setOperation(ComparisonOperations.IN_OR_NULL);
        personCriteria.setValue(Arrays.asList("Judge Linda").toArray());

        searchCriteriaList = Arrays.asList(startTimeCriteria, personCriteria);



        List<SearchCriteria> innerSearchCriteria = searchCriteriaList.stream().filter(sc -> innerKeys.contains(sc.getKey())).collect(Collectors.toList());
//        List<SearchCriteria> outerSearchCriteria = searchCriteriaList.stream().filter(sc -> outerKeys.contains(sc.getKey())).collect(Collectors.toList());
        String searchSelect = this.SEARCH_SESSION_QUERY;
        String innerWherePredicate = addInnerWherePredicates(innerSearchCriteria);
//        String outerWherePredicate = addOuterWherePredicates(outerSearchCriteria);
        searchSelect = searchSelect.replace("<WHERE_CLAUSES>", innerWherePredicate);
//        searchSelect = searchSelect.replace("<OUTER_WHERE>", outerWherePredicate);
        Query sqlQuery = entityManager.createNativeQuery(searchSelect);
        setParameters(sqlQuery, searchCriteriaList);

//        sqlQuery.
        val aaa = sqlQuery.getResultList();

        return null;

    }

    private String addInnerWherePredicates(List<SearchCriteria> searchCriteriaList) {
        StringBuilder innerWhere = new StringBuilder();

        for (SearchCriteria sc : searchCriteriaList) {
            String key = sc.getKey();
            switch (key){
//                case "startDate":
//                    innerWhere.append("AND session_start <= :startDate :: date ");
//                    break;
//                case "endDate":
//                    innerWhere.append(String.format("AND main_session.start >= %s ", sc.getValue().toString()));
//                    break;
//                case "sessionType":
//                    innerWhere.append(String.format("AND st.code IN (:sessionTypes) ", sc.getValue().toString()));
//                    break;
//                case "roomId":
//                    innerWhere.append(String.format("AND r.id IN (%s) ", sc.getValue().toString()));
//                    break;
//                case "personId":
//                    if (sc.getOperation() == ComparisonOperations.IN_OR_NULL) {
//                        innerWhere.append("AND person_id IN (:personId) OR person_id IS NULL");
//                    } else {
//                        innerWhere.append("AND person_id IN (:personId)");
//                    }
//                    break;
            }
        }

        return innerWhere.toString();
    }

    private void setParameters(Query query, List<SearchCriteria> searchCriteriaList) {

        for (SearchCriteria sc : searchCriteriaList) {
            String key = sc.getKey();
            switch (key){
//                case "startDate":
//                    query.setParameter("startDate", "2018-11-06 00:00:00"/*sc.getValue()*/);
//                    break;
//                case "endDate":
//                    innerWhere.append(String.format("AND main_session.start >= %s ", sc.getValue().toString()));
//                    break;
//                case "sessionType":
//                    innerWhere.append(String.format("AND st.code IN (:sessionTypes) ", sc.getValue().toString()));
//                    break;
//                case "roomId":
//                    innerWhere.append(String.format("AND r.id IN (%s) ", sc.getValue().toString()));
//                    break;
//                case "personId":
//                    List<String> ids = Arrays.asList("1143b1ea-1813-4acc-8b08-f37d1db59492");
//                    query.setParameter("personId", ids /*sc.getValue() */);
//                    break;
            }
        }
    }

//    private String addOuterWherePredicates(List<SearchCriteria> searchCriteriaList) {
//        String outerWhere = "";
//
//        for (SearchCriteria sc : searchCriteriaList) {
//            String key = sc.getKey();
//            switch (key){
//                case "listingDetails":
//                    String listingDetailOption = sc.getValue().toString();
//                    switch (listingDetailOption) {
//                        case "unListed":
//                           outerWhere += " AND utilisation = 100 ";
//                           break;
//                        case "partListed":
//                            outerWhere += " AND utilisation > 0 AND utilisation < 100 ";
//                            break;
//                        case "fullyListed":
//                            outerWhere += " AND utilisation = 100 ";
//                            break;
//                    }
//                    break;
//                case "listingDetailsCustomFrom":
//                    outerWhere += String.format(" AND utilisation > %s ", sc.getValue().toString());
//                    break;
//                case "listingDetailsCustomTo":
//                    outerWhere += String.format(" AND utilisation < %s ", sc.getValue().toString());
//                    break;
//            }
//
//        }
//
//        return outerWhere;
//    }

}
