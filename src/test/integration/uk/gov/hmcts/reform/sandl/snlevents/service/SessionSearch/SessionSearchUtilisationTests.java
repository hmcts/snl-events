package uk.gov.hmcts.reform.sandl.snlevents.service.SessionSearch;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.SearchSessionSelectColumn;
import uk.gov.hmcts.reform.sandl.snlevents.service.SessionSearch.BaseSessionSearchTests;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@Transactional
public class SessionSearchUtilisationTests extends BaseSessionSearchTests {

    @Test
    public void search_whenSessionHasNoHearingPartsAssigned_returnSessionWithUtilisationEqual0() {
        Session session = createSession(HALF_HOUR, UUID.randomUUID(), null, null, null);

        sessionRepository.saveAndFlush(session);

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, null, null);

        SessionSearchResponse sessionSearchResponse = sessions.getContent().get(0);
        assertEquals(0, sessionSearchResponse.getUtilisation().longValue());
    }

    @Test
    public void search_whenSessionHasSingleHearingPartsAssigned_UtilisationShouldIncludeAssignedHearingPart() {
        Session session = createSession(ONE_HOUR, UUID.randomUUID(), null, null, null);

        Hearing hearing = createHearing(HALF_HOUR, null, false);
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(UUID.randomUUID());
        hearingPart.setHearing(hearing);
        hearingPart.setSession(session);

        hearingRepository.saveAndFlush(hearing);
        sessionRepository.saveAndFlush(session);
        hearingPartRepository.saveAndFlush(hearingPart);

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, null, null);
        SessionSearchResponse sessionSearchResponse = sessions.getContent().get(0);
        assertEquals(50, sessionSearchResponse.getUtilisation().longValue());
    }

    @Test
    public void search_whenSessionHasMultipleSingleHearingPartsAssigned_UtilisationShouldIncludeAssignedHearingParts() {
        Session session = createSession(ONE_HOUR, UUID.randomUUID(), null, null, null);

        Hearing hearingA = createHearing(HALF_HOUR, null, false);
        HearingPart hearingPartA = new HearingPart();
        hearingPartA.setId(UUID.randomUUID());
        hearingPartA.setHearing(hearingA);
        hearingPartA.setSession(session);

        Hearing hearingB = createHearing(HALF_HOUR, null, false);
        HearingPart hearingPartB = new HearingPart();
        hearingPartB.setId(UUID.randomUUID());
        hearingPartB.setHearing(hearingB);
        hearingPartB.setSession(session);

        hearingRepository.saveAndFlush(hearingA);
        hearingRepository.saveAndFlush(hearingB);
        sessionRepository.saveAndFlush(session);
        hearingPartRepository.saveAndFlush(hearingPartA);
        hearingPartRepository.saveAndFlush(hearingPartB);

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, null, null);
        SessionSearchResponse sessionSearchResponse = sessions.getContent().get(0);
        assertEquals(100, sessionSearchResponse.getUtilisation().longValue());
    }

    @Test
    public void search_whenSessionHasOneMultiHearingPartAssigned_UtilisationShouldIncludeAssignedHearingParts() {
        UUID sessionIdA = UUID.randomUUID();
        Session sessionA = createSession(ONE_HOUR, sessionIdA, null, null, null);
        UUID sessionIdB = UUID.randomUUID();
        Session sessionB = createSession(TWO_HOUR, sessionIdB, null, null, null);

        Hearing hearing = createHearing(HALF_HOUR, null, true);
        HearingPart hearingPartA = new HearingPart();
        hearingPartA.setId(UUID.randomUUID());
        hearingPartA.setHearing(hearing);
        hearingPartA.setSession(sessionA);

        HearingPart hearingPartB = new HearingPart();
        hearingPartB.setId(UUID.randomUUID());
        hearingPartB.setHearing(hearing);
        hearingPartB.setSession(sessionB);

        hearingRepository.saveAndFlush(hearing);
        sessionRepository.saveAndFlush(sessionA);
        sessionRepository.saveAndFlush(sessionB);
        hearingPartRepository.saveAndFlush(hearingPartA);
        hearingPartRepository.saveAndFlush(hearingPartB);

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, SearchSessionSelectColumn.UTILISATION, Sort.Direction.ASC);

        assertEquals(100, sessions.getContent().get(0).getUtilisation().longValue());
        assertEquals(100, sessions.getContent().get(1).getUtilisation().longValue());
    }

    @Test
        public void search_whenSessionHasOneMultiAndOneSingleHearingPartAssigned_UtilisationShouldIncludeAssignedHearingParts() {
        UUID sessionIdA = UUID.randomUUID();
        Session sessionA = createSession(ONE_HOUR, sessionIdA, null, null, null);

        Hearing multipleHearing = createHearing(HALF_HOUR, null, true);
        HearingPart hearingPartA = new HearingPart();
        hearingPartA.setId(UUID.randomUUID());
        hearingPartA.setHearing(multipleHearing);
        hearingPartA.setSession(sessionA);

        Hearing singleHearing = createHearing(HALF_HOUR, null, false);
        HearingPart hearingPartB = new HearingPart();
        hearingPartB.setId(UUID.randomUUID());
        hearingPartB.setHearing(singleHearing);
        hearingPartB.setSession(sessionA);

        hearingRepository.saveAndFlush(multipleHearing);
        hearingRepository.saveAndFlush(singleHearing);
        sessionRepository.saveAndFlush(sessionA);
        hearingPartRepository.saveAndFlush(hearingPartA);
        hearingPartRepository.saveAndFlush(hearingPartB);

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, SearchSessionSelectColumn.UTILISATION, Sort.Direction.ASC);
        assertEquals(150, sessions.getContent().get(0).getUtilisation().longValue());
    }

    @Test
    public void search_whenHasTwoMultiHearingPartAssigned_UtilisationShouldBeDoubled() {
        UUID sessionIdA = UUID.randomUUID();
        Session sessionA = createSession(ONE_HOUR, sessionIdA, null, null, null);

        Hearing multipleHearing = createHearing(HALF_HOUR, null, true);
        HearingPart hearingPartA = new HearingPart();
        hearingPartA.setId(UUID.randomUUID());
        hearingPartA.setHearing(multipleHearing);
        hearingPartA.setSession(sessionA);

        Hearing singleHearing = createHearing(HALF_HOUR, null, true);
        HearingPart hearingPartB = new HearingPart();
        hearingPartB.setId(UUID.randomUUID());
        hearingPartB.setHearing(singleHearing);
        hearingPartB.setSession(sessionA);

        hearingRepository.saveAndFlush(multipleHearing);
        hearingRepository.saveAndFlush(singleHearing);
        sessionRepository.saveAndFlush(sessionA);
        hearingPartRepository.saveAndFlush(hearingPartA);
        hearingPartRepository.saveAndFlush(hearingPartB);

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, SearchSessionSelectColumn.UTILISATION, Sort.Direction.ASC);
        assertEquals(200, sessions.getContent().get(0).getUtilisation().longValue());
    }
}



