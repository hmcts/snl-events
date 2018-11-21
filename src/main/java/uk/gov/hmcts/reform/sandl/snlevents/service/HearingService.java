package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;
import java.util.UUID;

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

    public List<HearingForListingResponse> getHearingsForListing() {
        Query sqlQuery = entityManager.createNativeQuery(HEARING_FOR_LISTING_QUERY, "MapToHearingForListingResponse");
        return sqlQuery.getResultList();
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
