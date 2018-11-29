package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class HearingForListingQueries {

    private final String hearingForListingSelectCount = "SELECT Count(*) ";
    private final String hearingForListingSelectHearings = "SELECT h.id, case_number, case_title, "
        + "ct.code AS case_type_code, "
        + "ct.description AS case_type_description, ht.code AS hearing_type_code, "
        + "ht.description AS hearing_type_description, (duration * 1000000000) AS duration, "
        + "schedule_start, schedule_end, version, "
        + "priority, communication_facilitator, reserved_judge_id, p.name AS reserved_judge_name, number_of_sessions, "
        + "h.status, is_multisession ";

    private final String hearingForListingQueryBody = "FROM hearing h "
        + "LEFT JOIN person p ON p.id = h.reserved_judge_id "
        + "INNER JOIN case_type ct ON h.case_type_code = ct.code "
        + "INNER JOIN hearing_type ht ON h.hearing_type_code = ht.code "
        + "INNER JOIN status_config sc ON h.status = sc.status "
        + "WHERE can_be_listed = TRUE "
        + "AND h.status != 'Listed' "
        + "AND is_deleted = FALSE ";

    private String orderByQueryPart = "ORDER BY <order_property> <order_direction>";

    private final String orderPropertyPlaceholder = "<order_property>";
    private final String orderDirectionPlaceholder = "<order_direction>";

    private String getOrderByQueryPart(String property, String direction) {
        return orderByQueryPart
            .replace(orderPropertyPlaceholder, property)
            .replace(orderDirectionPlaceholder, direction);
    }

    public String getMainQuery(HearingForListingColumn property, Sort.Direction direction) {
        return hearingForListingSelectHearings + hearingForListingQueryBody
            + getOrderByQueryPart(property.getColumnName(), direction.toString());
    }

    public String getCountQuery() {
        return hearingForListingSelectCount + hearingForListingQueryBody;
    }
}
