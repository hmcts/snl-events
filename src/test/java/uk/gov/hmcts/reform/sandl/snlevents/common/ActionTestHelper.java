package uk.gov.hmcts.reform.sandl.snlevents.common;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.sandl.snlevents.StatusesMock;
import uk.gov.hmcts.reform.sandl.snlevents.actions.Action;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ActionTestHelper {
    private StatusesMock statusesMock = new StatusesMock();

    public HearingPart createHearingPartWithSession(UUID hpId, Long hpVersion, Hearing hearing, UUID sessionId,
                                                     Status hpStatus, OffsetDateTime dateTime) {
        HearingPart hearingPart = new HearingPart();
        hearingPart.setId(hpId);
        hearingPart.setHearing(hearing);
        hearingPart.setVersion(hpVersion);
        hearingPart.setStatus(statusesMock.statusConfigService.getStatusConfig(hpStatus));

        Session session = new Session();
        session.setId(sessionId);
        session.setStart(dateTime);

        hearingPart.setSessionId(sessionId);
        hearingPart.setSession(session);

        return hearingPart;
    }

    public void assertHearingPartsSessionIsSetToNull(Action action, HearingPartRepository hearingPartRepository) {
        action.getAndValidateEntities();
        action.act();

        ArgumentCaptor<List<HearingPart>> captor = ArgumentCaptor.forClass((Class) List.class);

        Mockito.verify(hearingPartRepository).save(captor.capture());
        captor.getValue().forEach(hp -> {
            assertNull(hp.getSessionId());
            assertNull(hp.getSession());
        });
    }

    public void assertThat_getAssociatedEntitiesIds_returnsCorrectIds(Action action, UUID[] expectedUuids) {
        action.getAndValidateEntities();
        UUID[] ids = action.getAssociatedEntitiesIds();

        assertThat(ids.length).isEqualTo(expectedUuids.length);
        assertTrue(Arrays.asList(ids).containsAll(Arrays.asList(expectedUuids)));
    }
}
