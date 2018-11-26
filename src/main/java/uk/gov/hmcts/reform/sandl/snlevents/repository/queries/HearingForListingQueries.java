package uk.gov.hmcts.reform.sandl.snlevents.repository.queries;

import org.springframework.stereotype.Component;

@Component
public class HearingForListingQueries {

    private final String hearingForListingSelectCount = "SELECT Count(*) ";
    private final String hearingForListingSelectHearings = "SELECT h.id, case_number, case_title, ct.code as case_type_code, "
        + "ct.description as case_type_description, ht.code as hearing_type_code, "
        + "ht.description as hearing_type_description, (duration * 1000000000) as duration, "
        + "schedule_start, schedule_end, version, "
        + "priority, communication_facilitator, reserved_judge_id, p.name as reserved_judge_name, number_of_sessions, "
        + "h.status, is_multisession ";

    private final String hearingForListingQueryBody = "FROM hearing h "
        + "LEFT JOIN person p on p.id = h.reserved_judge_id "
        + "INNER JOIN case_type ct on h.case_type_code = ct.code "
        + "INNER JOIN hearing_type ht on h.hearing_type_code = ht.code "
        + "INNER JOIN status_config sc on h.status = sc.status "
        + "WHERE can_be_listed = true "
        + "AND h.status != 'Listed' "
        + "AND is_deleted = false ";

    private String orderByQueryPart = "ORDER BY <order_property> <order_direction>";

    public String getOrderByQueryPart(String property, String direction) {
        return orderByQueryPart
            .replace("<order_property>", property)
            .replace("<order_direction>", direction);
    }

    public String getMainQuery(String property, String direction) {
        return hearingForListingSelectHearings + hearingForListingQueryBody + getOrderByQueryPart(property, direction);
    }

    public String getCountQuery() {
        return hearingForListingSelectCount + hearingForListingQueryBody;
    }
}
