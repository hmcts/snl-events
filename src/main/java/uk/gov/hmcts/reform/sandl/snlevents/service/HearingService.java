package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import static uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository.HEARING_FOR_LISTING_COUNT_QUERY;
import static uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository.HEARING_FOR_LISTING_QUERY;

@Service
public class HearingService {

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

    public Page<HearingForListingResponse> getHearingsForListing(Optional<Integer> page, Optional<Integer> size) {
        Query sqlQuery = entityManager.createNativeQuery(HEARING_FOR_LISTING_QUERY, "MapToHearingForListingResponse");
        sqlQuery.setFirstResult(page.orElseGet(() -> Integer.valueOf(1)) * size.orElseGet(() -> Integer.valueOf(100)));
        sqlQuery.setMaxResults(size.orElseGet(() -> Integer.valueOf(100)));

        BigInteger totalCount = getHearingsForListingCount();

        return new PageImpl<HearingForListingResponse>(sqlQuery.getResultList(), null, totalCount.longValue());
    }

    public BigInteger getHearingsForListingCount() {
        Query sqlQuery = entityManager.createNativeQuery(HEARING_FOR_LISTING_COUNT_QUERY);

        return (BigInteger) sqlQuery.getSingleResult();
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
