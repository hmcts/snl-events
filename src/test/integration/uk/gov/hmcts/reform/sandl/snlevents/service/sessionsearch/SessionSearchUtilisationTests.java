package uk.gov.hmcts.reform.sandl.snlevents.service.sessionsearch;

import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SessionSearchResponse;
import uk.gov.hmcts.reform.sandl.snlevents.repository.queries.sessionsearch.SearchSessionSelectColumn;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import javax.transaction.Transactional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Transactional
public class SessionSearchUtilisationTests extends BaseSessionSearchTest {

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

        assertSessionSearchResponse(sessionSearchResponse, 50, ONE_HOUR, 1, HALF_HOUR);
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

        assertSessionSearchResponse(sessionSearchResponse, 100, ONE_HOUR, 2, ONE_HOUR);
    }

    @Test
    public void search_whenSessionHasOneMultiHearingPartAssigned_UtilisationShouldBeCountedProperly() {
        UUID sessionIdA = UUID.randomUUID();
        Session sessionA = createSession(ONE_HOUR, sessionIdA, null, null, null);
        UUID sessionIdB = UUID.randomUUID();
        Session sessionB = createSession(TWO_HOURS, sessionIdB, null, null, null);
        sessionRepository.saveAndFlush(sessionA);
        sessionRepository.saveAndFlush(sessionB);

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
        hearingPartRepository.saveAndFlush(hearingPartA);
        hearingPartRepository.saveAndFlush(hearingPartB);

        Page<SessionSearchResponse> sessions = sessionService.searchForSession(
            Collections.emptyList(), FIRST_PAGE, SearchSessionSelectColumn.UTILISATION, Sort.Direction.ASC);

        assertSessionSearchResponse(sessions.getContent().get(0), 100, ONE_HOUR, 1, ONE_HOUR);
        assertSessionSearchResponse(sessions.getContent().get(1), 100, TWO_HOURS, 1, TWO_HOURS);

    }

    @Test
        public void search_whenSessionHasMultiAndSingleHearingPartAssigned_UtilisationShouldBeCountedProperly() {
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

        assertSessionSearchResponse(sessions.getContent().get(0), 150, ONE_HOUR, 2, ONE_AND_HALF_HOUR);
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

        assertSessionSearchResponse(sessions.getContent().get(0), 200, ONE_HOUR, 2, TWO_HOURS);
    }

    private void assertSessionSearchResponse(SessionSearchResponse sessionSearchResponse,
                                             long expectedUtilisation,
                                             Duration expectedDuration,
                                             int expectedHearingPartsCount,
                                             Duration expectedAllocatedDuration) {
        assertEquals(expectedUtilisation, sessionSearchResponse.getUtilisation().longValue());
        assertTrue(sessionSearchResponse.getDuration().equals(expectedDuration));
        assertEquals(expectedHearingPartsCount, sessionSearchResponse.getNoOfHearingPartsAssignedToSession());
        assertEquals(expectedAllocatedDuration.getSeconds(), sessionSearchResponse.getAllocatedDuration().getSeconds());
    }
}
