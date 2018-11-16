package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.math.BigInteger;
import java.util.List;

@Component
public class SearchSessionQuery {

    String COUNT_BY_SESSION_ID = "SELECT COUNT(session_id) FROM (<SELECT_QUERY>) AS FILTERED_SESSION_COUNT;";

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
        + "                st.description                                    AS session_type_description,\n"
        + "                main_session.id                                   AS session_id,\n"
        + "                main_session.start                                AS session_startTime,\n"
        + "                main_session.start                                AS session_startDate,\n"
        + "                main_session.duration                             AS session_duration,\n"
        + "                (SELECT count(id)\n"
        + "                 FROM hearing_part\n"
        + "                 WHERE session_id = main_session.id)              AS no_of_hearing_parts,\n"
        + "                (SELECT SUM(CASE WHEN h.is_multisession = TRUE THEN\n"
        + "                              CASE WHEN s.duration IS NULL THEN 0 ELSE s.duration END\n"
        + "                            ELSE\n"
        + "                              CASE WHEN h.duration IS NULL THEN 0 ELSE h.duration END\n"
        + "                           END)\n"
        + "                 FROM hearing_part\n"
        + "                   RIGHT JOIN session s ON hearing_part.session_id = s.id\n"
        + "                   LEFT JOIN hearing h ON hearing_part.hearing_id = h.id\n"
        + "                 WHERE s.id = main_session.id) AS allocated_duration\n"
        + "              FROM session main_session\n"
        + "                LEFT JOIN person p ON main_session.person_id = p.id\n"
        + "                LEFT JOIN room r ON main_session.room_id = r.id\n"
        + "                LEFT JOIN session_type st ON main_session.session_type_code = st.code\n"
        + "            ) AS MAIN_SELECT\n"
        + "     ) AS OUTER_SELECT\n"
        + "  <WHERE_CLAUSES>\n"
        + "  <ORDER_BY>\n"
        + "  <LIMIT>";

    String LIMIT_QUERY = "LIMIT :itemsPerPage OFFSET :pageNumber";

    @Autowired
    private EntityManager entityManager;

    public Page<SessionSearchResponse> search(List<SearchCriteria> searchCriteriaList, Pageable pageable, SearchSessionSelectColumn orderByColumn, Sort.Direction direction) {
        String whereClause = createWherePredicate(searchCriteriaList);
        String selectSessions = this.SEARCH_SESSION_QUERY.replace("<WHERE_CLAUSES>", whereClause);

        Long totalCount = getTotalCount(searchCriteriaList, selectSessions);

        String orderByClause = createOrderBy(orderByColumn, direction);
        selectSessions = selectSessions.replace("<LIMIT>", LIMIT_QUERY).replace("<ORDER_BY>", orderByClause);
        Query sqlQuery = entityManager.createNativeQuery(selectSessions, "MapToSessionSearchResponse");

        setQueryParameters(sqlQuery, searchCriteriaList);

        sqlQuery.setParameter("itemsPerPage", pageable.getPageSize());
        sqlQuery.setParameter("pageNumber", pageable.getOffset());

        List<SessionSearchResponse> queryResults = sqlQuery.getResultList();

        return new PageImpl<>(queryResults, pageable, totalCount.longValue());
    }

    private Long getTotalCount(List<SearchCriteria> searchCriteriaList, String selectSessions) {
        String selectForCount = selectSessions.replace("<ORDER_BY>", "").replace("<LIMIT>", "");
        String selectCount = this.COUNT_BY_SESSION_ID.replace("<SELECT_QUERY>", selectForCount);
        Query countALLQuery = entityManager.createNativeQuery(selectCount);
        setQueryParameters(countALLQuery, searchCriteriaList);

        return ((BigInteger)countALLQuery.getSingleResult()).longValue();
    }

    private void verifyPassedSearchCriterions(SearchSessionKey ssk, SearchCriteria sc) {
        if(ssk == null) {
            throw new SnlRuntimeException("Can't convert given key: '" + sc.getKey() + "' to SearchSessionKey");
        }
    }

    private String createOrderBy(SearchSessionSelectColumn orderByColumn, Sort.Direction direction) {
        if (orderByColumn != null && direction != null) {
            return "ORDER BY " + orderByColumn.getColumnName() + " " + direction.toString();
        }

        return "";
    }

    private String createWherePredicate(List<SearchCriteria> searchCriteriaList) {
        StringBuilder wherePredicate = new StringBuilder();

        searchCriteriaList.forEach(sc -> {
            SearchSessionKey ssk = SearchSessionKey.fromString(sc.getKey(), sc.getValue());
            verifyPassedSearchCriterions(ssk, sc);

            wherePredicate.append(ssk.getWherePredicate(sc));
        });

        // REMOVE first AND or OR
        String finalWherePredicate = cutOffFirstConjunction(wherePredicate.toString());
        if (finalWherePredicate.length() > 0) {
            finalWherePredicate = "WHERE " + finalWherePredicate;
        }
        return finalWherePredicate;
    }

    private String cutOffFirstConjunction(String wherePredicate) {
        String trimmedWherePredicate = wherePredicate.trim();
        if (trimmedWherePredicate.startsWith("AND")) {
            return trimmedWherePredicate.replaceFirst("AND", "");
        } else if (trimmedWherePredicate.startsWith("OR")) {
            return trimmedWherePredicate.replaceFirst("OR", "");
        }

        return trimmedWherePredicate;
    }

    private void setQueryParameters(Query query, List<SearchCriteria> searchCriteriaList) {
        searchCriteriaList.forEach(sc -> {
            SearchSessionKey ssk = SearchSessionKey.fromString(sc.getKey(), sc.getValue());
            verifyPassedSearchCriterions(ssk, sc);

            if (ssk.isParamNeeded()) {
                query.setParameter(ssk.getKey(), ssk.getValue());
            }
        });
    }
}
