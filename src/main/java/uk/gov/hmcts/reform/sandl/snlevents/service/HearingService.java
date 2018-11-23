package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.UnlistHearingAction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingForListingResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponseForAmendment;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.HearingQueries;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Service
public class HearingService {

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

    @Autowired
    private HearingQueries hearingQueries;

    @Autowired
    private HearingRepository hearingRepository;

    @Autowired
    private HearingPartRepository hearingPartRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActionService actionService;
    @Autowired
    private StatusConfigService statusConfigService;
    @Autowired
    private StatusServiceManager statusServiceManager;

    @Autowired
    private EntityManager entityManager;

    public HearingSearchResponseForAmendment get(UUID id) {
        return hearingQueries.get(id);
    }

    public Page<HearingSearchResponse> search(List<SearchCriteria> searchCriteriaList, Pageable pageable) {
        return hearingQueries.search(searchCriteriaList, pageable);
    }

    public Page<HearingForListingResponse> getHearingsForListing(Optional<Integer> page,
                                                                 Optional<Integer> size,
                                                                 Optional<String> sortByProperty,
                                                                 Optional<String> sortByDirection) {
        String orderByQueryPartWithParams = orderByQueryPart
            .replace("<order_property>", sortByProperty.orElse("case_number"))
            .replace("<order_direction>", sortByDirection.orElse("asc"));

        String query = hearingForListingSelectHearings + hearingForListingQueryBody + orderByQueryPartWithParams;
        Query sqlQuery = entityManager.createNativeQuery(query, "MapToHearingForListingResponse");

        sqlQuery.setFirstResult(page.orElse(Integer.valueOf(1)) * size.orElse(Integer.valueOf(100)));
        sqlQuery.setMaxResults(size.orElse(Integer.valueOf(100)));

        String countQuery = hearingForListingSelectCount + hearingForListingQueryBody;
        Query sqlCountQuery = entityManager.createNativeQuery(countQuery);

        BigInteger totalCount = (BigInteger) sqlCountQuery.getSingleResult();

        return new PageImpl<HearingForListingResponse>(sqlQuery.getResultList(), null, totalCount.longValue());
    }

    public UserTransaction unlist(UnlistHearingRequest unlistHearingRequest) {
        Action action = new UnlistHearingAction(
            unlistHearingRequest,
            hearingRepository,
            hearingPartRepository,
            statusConfigService,
            statusServiceManager,
            objectMapper
        );

        return actionService.execute(action);
    }
}
