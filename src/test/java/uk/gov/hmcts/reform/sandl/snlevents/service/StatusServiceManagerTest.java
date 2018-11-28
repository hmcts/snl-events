package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Statusable;

import java.time.OffsetDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
public class StatusServiceManagerTest {
    private static final Long VERSION = 0L;
    private StatusServiceManager statusServiceManager = new StatusServiceManager();

    @Test
    public void shouldGiveProperValues_whenEntityHasListedStatus() {
        Statusable entity = createHearingWithStatus(createListedStatus());

        assertThat(statusServiceManager.canBeUnlisted(entity)).isEqualTo(true);
        assertThat(statusServiceManager.canBeListed(entity)).isEqualTo(false);
        assertThat(statusServiceManager.shouldBeCountInUtilization(entity)).isEqualTo(true);
    }

    @Test
    public void shouldGiveProperValues_whenEntityHasUnlistedStatus() {
        Statusable entity = createHearingPartWithStatus(createUnlistedStatus());

        assertThat(statusServiceManager.canBeUnlisted(entity)).isEqualTo(false);
        assertThat(statusServiceManager.canBeListed(entity)).isEqualTo(true);
        assertThat(statusServiceManager.shouldBeCountInUtilization(entity)).isEqualTo(false);
    }

    @Test
    public void canAdjournIsTrue_whenHearingHasListedStatus_andListingDateIsBeforeNow() {
        Hearing hearing = createHearingWithStatus(createListedStatus());

        Session session = new Session();
        session.setStart(OffsetDateTime.now().minusDays(5));

        HearingPart hearingPart = new HearingPart();
        hearingPart.setSession(session);


        hearing.setHearingParts(Arrays.asList(hearingPart));

        assertThat(statusServiceManager.canBeAdjourned(hearing)).isEqualTo(true);
    }

    @Test
    public void canAdjournIsFalse_whenHearingHasUnlistedStatus() {
        Hearing hearing = createHearingWithStatus(createUnlistedStatus());

        assertThat(statusServiceManager.canBeAdjourned(hearing)).isEqualTo(false);
    }

    @Test
    public void canWithdrawIsFalse_whenHearingHasAdjournedStatus() {
        Hearing hearing = createHearingWithStatus(createAdjournedStatus());

        assertThat(statusServiceManager.canBeWithdrawn(hearing)).isEqualTo(false);
    }

    private Hearing createHearingWithStatus(StatusConfig status) {
        val hearing = new Hearing();
        hearing.setVersion(VERSION);
        hearing.setStatus(status);

        return hearing;
    }

    private HearingPart createHearingPartWithStatus(StatusConfig status) {
        val hearingPart = new HearingPart();
        hearingPart.setStatus(status);

        return hearingPart;
    }

    private StatusConfig createListedStatus() {
        val status = new StatusConfig();
        status.setStatus(Status.Listed);
        status.setCanBeListed(false);
        status.setCanBeUnlisted(true);
        status.setCanBeAdjourned(true);
        status.setCanBeVacated(true);
        status.setCanBeWithdrawn(true);
        status.setCountInUtilization(true);

        return status;
    }

    private StatusConfig createUnlistedStatus() {
        val status = new StatusConfig();
        status.setStatus(Status.Unlisted);
        status.setCanBeListed(true);
        status.setCanBeUnlisted(false);
        status.setCanBeAdjourned(false);
        status.setCanBeVacated(false);
        status.setCanBeWithdrawn(true);
        status.setCountInUtilization(false);

        return status;
    }

    private StatusConfig createAdjournedStatus() {
        val status = new StatusConfig();
        status.setStatus(Status.Adjourned);
        status.setCanBeListed(false);
        status.setCanBeUnlisted(false);
        status.setCanBeAdjourned(false);
        status.setCanBeVacated(false);
        status.setCanBeWithdrawn(false);
        status.setCountInUtilization(false);

        return status;
    }
}
