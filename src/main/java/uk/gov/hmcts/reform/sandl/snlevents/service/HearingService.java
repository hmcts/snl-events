package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.UnlistHearingAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.WithdrawHearingAction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.UnlistHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingForListingResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.WithdrawHearingRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingSearchResponseForAmendment;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.HearingForListingQueries;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.HearingQueries;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchCriteria;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.EntityManager;

@Service
public class HearingService {

    @Autowired
    private HearingQueries hearingQueries;

    @Autowired
    private HearingForListingQueries hearingForListingQueries;

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

        String hearingForListingQuery = hearingForListingQueries.getMainQuery(
            sortByProperty.orElse("case_number"), sortByDirection.orElse("asc"));

        Query sqlQuery = entityManager.createNativeQuery(hearingForListingQuery, "MapToHearingForListingResponse");
        sqlQuery.setFirstResult(page.orElse(Integer.valueOf(1)) * size.orElse(Integer.valueOf(100)));
        sqlQuery.setMaxResults(size.orElse(Integer.valueOf(100)));

        String countQuery = hearingForListingQueries.getCountQuery();
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

    public UserTransaction withdraw(WithdrawHearingRequest withdrawHearingRequest) {
        Action action = new WithdrawHearingAction(
            withdrawHearingRequest,
            hearingRepository,
            hearingPartRepository,
            statusConfigService,
            statusServiceManager,
            objectMapper,
            entityManager
        );

        return actionService.execute(action);
    }
}
