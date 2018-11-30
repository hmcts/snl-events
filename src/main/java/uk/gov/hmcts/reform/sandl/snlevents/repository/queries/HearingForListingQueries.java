package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class HearingForListingQueries {

    private static final String HEARING_FOR_LISTING_SELECT_COUNT = "SELECT Count(*) ";
    private static final String HEARING_FOR_LISTING_SELECT_ENTITIES = "SELECT h.id, case_number, case_title, "
        + "ct.code AS case_type_code, "
        + "ct.description AS case_type_description, ht.code AS hearing_type_code, "
        + "ht.description AS hearing_type_description, (duration * 1000000000) AS duration, "
        + "schedule_start, schedule_end, version, "
        + "priority, communication_facilitator, reserved_judge_id, p.name AS reserved_judge_name, number_of_sessions, "
        + "h.status, is_multisession ";

    private static final String HEARING_FOR_LISTING_QUERY_BODY = "FROM hearing h "
        + "LEFT JOIN person p ON p.id = h.reserved_judge_id "
        + "INNER JOIN case_type ct ON h.case_type_code = ct.code "
        + "INNER JOIN hearing_type ht ON h.hearing_type_code = ht.code "
        + "INNER JOIN status_config sc ON h.status = sc.status "
        + "WHERE can_be_listed = TRUE "
        + "AND h.status != 'Listed' "
        + "AND is_deleted = FALSE ";

    private static final String ORDER_BY_QUERY_PART = "ORDER BY <order_property> <order_direction>";

    private static final String ORDER_PROPERTY_PLACEHOLDER = "<order_property>";
    private static final String ORDER_DIRECTION_PLACEHOLDER = "<order_direction>";

    public String getMainQuery(HearingForListingColumn property, Sort.Direction direction) {
        return HEARING_FOR_LISTING_SELECT_ENTITIES + HEARING_FOR_LISTING_QUERY_BODY
            + getOrderByQueryPart(property.getColumnName(), direction.toString());
    }

    public String getCountQuery() {
        return HEARING_FOR_LISTING_SELECT_COUNT + HEARING_FOR_LISTING_QUERY_BODY;
    }

    private String getOrderByQueryPart(String property, String direction) {
        return ORDER_BY_QUERY_PART
            .replace(ORDER_PROPERTY_PLACEHOLDER, property)
            .replace(ORDER_DIRECTION_PLACEHOLDER, direction);
    }
}
