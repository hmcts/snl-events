package uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Component
public class SearchSessionQuery {
    private static final String COUNT_BY_SESSION_ID = "SELECT COUNT(session_id) FROM (<SELECT_QUERY>) "
        + "AS FILTERED_SESSION_COUNT;";
    private static final String SELECT_QUERY_PLACEHOLDER = "<SELECT_QUERY>";
    // multiply duration fields by 1000 000 000 in order to convert them from nanoseconds to seconds
    private static final String SEARCH_SESSION_QUERY = "SELECT\n"
        + "  *,\n"
        + "  CASE WHEN utilisation > 100\n"
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
        + "                main_session.duration * 1000000000                AS session_duration,\n"
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
        + "                 WHERE s.id = main_session.id) * 1000000000       AS allocated_duration\n"
        + "              FROM session main_session\n"
        + "                LEFT JOIN person p ON main_session.person_id = p.id\n"
        + "                LEFT JOIN room r ON main_session.room_id = r.id\n"
        + "                LEFT JOIN session_type st ON main_session.session_type_code = st.code\n"
        + "            ) AS MAIN_SELECT\n"
        + "     ) AS OUTER_SELECT\n"
        + "  <WHERE_CLAUSES>\n"
        + "  <ORDER_BY>\n"
        + "  <LIMIT>";
    private static final String LIMIT_QUERY = "LIMIT :itemsPerPage OFFSET :pageNumber";
    private static final String ITEMS_PER_PAGE_PARAM_NAME = "itemsPerPage";
    private static final String PAGE_NUMBER_PARAM_NAME = "pageNumber";
    private static final String LIMIT_PLACEHOLDER = "<LIMIT>";
    private static final String WHERE_PLACEHOLDER = "<WHERE_CLAUSES>";
    private static final String ORDER_BY_PLACEHOLDER = "<ORDER_BY>";
    private static final List<String> SEARCH_SESSION_QUERY_PLACEHOLDERS = Arrays.asList(
        LIMIT_PLACEHOLDER,
        WHERE_PLACEHOLDER,
        ORDER_BY_PLACEHOLDER
    );

    @Autowired
    private EntityManager entityManager;

    public Page<SessionSearchResponse> search(List<SearchCriteria> searchCriteriaList,
                                              Pageable pageable,
                                              SearchSessionSelectColumn orderByColumn,
                                              Sort.Direction direction) {
        List<SessionFilterKey> sessionFilterKeys = mapToSessionFilterKeys(searchCriteriaList);
        List<WhereClauseInfo> whereClauseInfos = sessionFilterKeys.stream()
            .map(SessionFilterKey::getWherePredicate)
            .collect(Collectors.toList());

        String whereClause = createWherePredicate(whereClauseInfos);
        String selectSessions = SEARCH_SESSION_QUERY.replace(WHERE_PLACEHOLDER, whereClause);

        Long totalCount = getTotalCount(whereClauseInfos, selectSessions);

        List<SessionSearchResponse> queryResults = searchSessions(
            selectSessions,
            whereClauseInfos,
            pageable,
            orderByColumn,
            direction
        );

        return new PageImpl<>(queryResults, pageable, totalCount);
    }

    private List<SessionSearchResponse> searchSessions(String selectSessions,
                                                       List<WhereClauseInfo> whereClauseInfos,
                                                       Pageable pageable,
                                                       SearchSessionSelectColumn orderByColumn,
                                                       Sort.Direction direction) {
        String orderByClause = createOrderBy(orderByColumn, direction);
        selectSessions = selectSessions
            .replace(LIMIT_PLACEHOLDER, LIMIT_QUERY)
            .replace(ORDER_BY_PLACEHOLDER, orderByClause);


        Query sqlQuery = entityManager.createNativeQuery(selectSessions, "MapToSessionSearchResponse");

        setWhereQueryParameters(sqlQuery, whereClauseInfos);
        sqlQuery.setParameter(ITEMS_PER_PAGE_PARAM_NAME, pageable.getPageSize());
        sqlQuery.setParameter(PAGE_NUMBER_PARAM_NAME, pageable.getOffset());

        return (List<SessionSearchResponse>) sqlQuery.getResultList();
    }

    private Long getTotalCount(List<WhereClauseInfo> whereClauseInfos, String selectSessions) {
        // Clear all that are not fullfilled placeholders
        String selectForCount = selectSessions;
        for (String placeHolder : SEARCH_SESSION_QUERY_PLACEHOLDERS) {
            selectForCount = selectForCount.replace(placeHolder, "");
        }

        // Create and execute SELECT COUNT statement
        String selectCount = COUNT_BY_SESSION_ID.replace(SELECT_QUERY_PLACEHOLDER, selectForCount);
        Query countAllQuery = entityManager.createNativeQuery(selectCount);
        setWhereQueryParameters(countAllQuery, whereClauseInfos);

        return ((BigInteger) countAllQuery.getSingleResult()).longValue();
    }

    private List<SessionFilterKey> mapToSessionFilterKeys(List<SearchCriteria> searchCriteriaList) {
        return searchCriteriaList.stream().map(sc -> {
            SessionFilterKey ssk = SessionFilterKey.fromSearchCriteria(sc);

            if (ssk == null) {
                throw new SnlRuntimeException("Can't convert given key: '" + sc.getKey() + "' to SessionFilterKey");
            }

            return ssk;
        }).collect(Collectors.toList());
    }

    private String createOrderBy(SearchSessionSelectColumn orderByColumn, Sort.Direction direction) {
        if (orderByColumn != null && direction != null) {
            return "ORDER BY " + orderByColumn.getColumnName() + " " + direction.toString();
        }

        return "";
    }

    private String createWherePredicate(List<WhereClauseInfo> whereClauseInfos) {
        List<WhereClauseInfo> othersWhereCauseInfo = whereClauseInfos.stream().filter(wci ->
            !SessionFilterKey.UTILISATION_KEYS.contains(wci.getSessionFilterKey().getKey())
        ).collect(Collectors.toList());

        // Utilisation options are join using OR conjunction and merged with other using AND conjunction
        List<WhereClauseInfo> utilisationWhereCauseInfo = whereClauseInfos.stream().filter(wci ->
            SessionFilterKey.UTILISATION_KEYS.contains(wci.getSessionFilterKey().getKey())
        ).collect(Collectors.toList());

        String andJoinedWherePredicate = getSqlWhereString(othersWhereCauseInfo);

        if (!utilisationWhereCauseInfo.isEmpty()) {

            if (!othersWhereCauseInfo.isEmpty()) {
                andJoinedWherePredicate += " AND ";
            }

            String utilisationWhereClause = getSqlWhereString(utilisationWhereCauseInfo);
            andJoinedWherePredicate += String.format(" ( %s ) ", utilisationWhereClause);
        }

        if (!andJoinedWherePredicate.isEmpty()) {
            andJoinedWherePredicate = "WHERE " + andJoinedWherePredicate;
        }

        return andJoinedWherePredicate;
    }

    private String getSqlWhereString(List<WhereClauseInfo> othersWhereCauseInfo) {
        StringBuilder andWherePredicate = new StringBuilder();
        othersWhereCauseInfo.forEach(wci -> andWherePredicate.append(wci.getWhereClause()));
        return cutOffFirstConjunction(andWherePredicate.toString());
    }

    private String cutOffFirstConjunction(String wherePredicate) {
        final String and = "AND";
        final String or = "OR";
        String trimmedWherePredicate = wherePredicate.trim();

        if (trimmedWherePredicate.startsWith(and)) {
            return trimmedWherePredicate.replaceFirst(and, "");
        } else if (trimmedWherePredicate.startsWith(or)) {
            return trimmedWherePredicate.replaceFirst(or, "");
        }

        return trimmedWherePredicate;
    }

    private void setWhereQueryParameters(Query query, List<WhereClauseInfo> whereClauseInfos) {
        whereClauseInfos.forEach(wci -> wci.getKeyValuePairs().forEach(query::setParameter));
    }
}
