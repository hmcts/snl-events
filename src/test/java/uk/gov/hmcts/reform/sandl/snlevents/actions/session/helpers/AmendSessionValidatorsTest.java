package uk.gov.hmcts.reform.sandl.snlevents.actions.session.helpers;

import org.junit.Assert;
import org.junit.Test;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.StatusConfig;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DragAndDropSessionRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

public class AmendSessionValidatorsTest {

    private final Duration duration = Duration.ofMinutes(30);
    private final OffsetDateTime sessionStartTime = OffsetDateTime.now();

    @Test
    public void validateIfJudgeCanBeChanged_shouldDoNotThrowAnException_WhenSessionHasNotAnyMultiSessionHearing() {
        Session session = createSessionWithMultiSessionHearing();
        assertDoNotThrow(() ->
            AmendSessionValidators.validateIfJudgeCanBeChanged.accept(session, new DragAndDropSessionRequest())
        );
    }

    @Test(expected = SnlRuntimeException.class)
    public void validateIfJudgeCanBeChanged_shouldThrowException_WhenSessionHasMultiSessionHearingAndJudgeHasChanged() {
        Hearing hearing = new Hearing();
        hearing.setMultiSession(true);

        HearingPart hearingPart = new HearingPart();
        hearingPart.setHearing(hearing);

        Person personA = new Person();
        personA.setId(UUID.randomUUID());

        Session session = new Session();
        session.addHearingPart(hearingPart);
        session.setPerson(personA);

        DragAndDropSessionRequest dragAndDropSessionRequest = new DragAndDropSessionRequest();
        dragAndDropSessionRequest.setPersonId(UUID.randomUUID());

        AmendSessionValidators.validateIfJudgeCanBeChanged.accept(session, dragAndDropSessionRequest);
    }

    @Test
    public void validateStartAndEndDateWasShrinked_shouldDoNotThrowAnException_WhenSessionHasNotAnyListedHearingPart() {
        Session session = createSessionWithUnlistedHearingPart();
        assertDoNotThrow(() ->
            AmendSessionValidators.validateStartAndEndDateWasShrinked.accept(session, new DragAndDropSessionRequest())
        );
    }

    @Test(expected = SnlRuntimeException.class)
    public void validateStartAndEndDateWasShrinked_shouldThrowException_WhenSessionStartTimeHasShrinked() {
        Session session = createSessionWithListedHearingPart();

        DragAndDropSessionRequest dragAndDropSessionRequest = new DragAndDropSessionRequest();
        dragAndDropSessionRequest.setStart(sessionStartTime.plusMinutes(10));
        dragAndDropSessionRequest.setDurationInSeconds(duration.getSeconds());

        AmendSessionValidators.validateStartAndEndDateWasShrinked.accept(session, dragAndDropSessionRequest);
    }

    @Test(expected = SnlRuntimeException.class)
    public void validateStartAndEndDateWasShrinked_shouldThrowException_WhenSessionEndTimeHasShrinked() {
        Session session = createSessionWithListedHearingPart();

        DragAndDropSessionRequest dragAndDropSessionRequest = new DragAndDropSessionRequest();
        dragAndDropSessionRequest.setStart(sessionStartTime);
        dragAndDropSessionRequest.setDurationInSeconds(duration.minusMinutes(5).getSeconds());

        AmendSessionValidators.validateStartAndEndDateWasShrinked.accept(session, dragAndDropSessionRequest);
    }

    @Test
    public void canChangeDay_shouldDoNotThrowAnException_WhenSessionHasNotAnyListedHearingPart() {
        Session session = createSessionWithUnlistedHearingPart();
        assertDoNotThrow(() ->
            AmendSessionValidators.canChangeDay.accept(session, new DragAndDropSessionRequest())
        );
    }

    @Test(expected = SnlRuntimeException.class)
    public void canChangeDay_shouldThrowException_WhenSessionStartTimeHasChangeDay() {
        Session session = createSessionWithListedHearingPart();

        DragAndDropSessionRequest dragAndDropSessionRequest = new DragAndDropSessionRequest();
        dragAndDropSessionRequest.setStart(sessionStartTime.plusDays(1));
        dragAndDropSessionRequest.setDurationInSeconds(duration.getSeconds());

        AmendSessionValidators.canChangeDay.accept(session, dragAndDropSessionRequest);
    }

    @Test
    public void validateStartIsSetEarlier_shouldDoNotThrowAnException_WhenSessionHasNotAnyListedHearingPart() {
        Session session = createSessionWithUnlistedHearingPart();
        assertDoNotThrow(() ->
            AmendSessionValidators.validateStartIsSetEarlier.accept(session, new AmendSessionRequest())
        );
    }

    @Test(expected = SnlRuntimeException.class)
    public void validateStartIsSetEarlier_shouldThrowException_WhenSessionStartTimeHasChangeDay() {
        Session session = createSessionWithListedHearingPart();

        AmendSessionRequest amendSessionRequest = new AmendSessionRequest();
        amendSessionRequest.setStartTime(sessionStartTime.plusDays(1));

        AmendSessionValidators.validateStartIsSetEarlier.accept(session, amendSessionRequest);
    }

    @Test
    public void validateDurationIsGreaterOrEqual_shouldDoNotThrowAnException_WhenSessionHasNotAnyListedHearingPart() {
        Session session = createSessionWithUnlistedHearingPart();
        assertDoNotThrow(() ->
            AmendSessionValidators.validateDurationIsGreaterOrEqual.accept(session, new AmendSessionRequest())
        );
    }

    @Test(expected = SnlRuntimeException.class)
    public void validateDurationIsGreaterOrEqual_shouldThrowException_WhenSessionStartTimeHasChangeDay() {
        Session session = createSessionWithListedHearingPart();

        AmendSessionRequest amendSessionRequest = new AmendSessionRequest();
        amendSessionRequest.setDurationInSeconds(duration.minusSeconds(1));

        AmendSessionValidators.validateDurationIsGreaterOrEqual.accept(session, amendSessionRequest);
    }


    private Session createSessionWithListedHearingPart() {
        return createSession(Status.Listed);
    }

    private Session createSessionWithUnlistedHearingPart() {
        return createSession(Status.Unlisted);
    }

    private Session createSession(Status hearingPartStatus) {
        StatusConfig statusConfig = new StatusConfig();
        statusConfig.setStatus(hearingPartStatus);

        HearingPart hearingPart = new HearingPart();
        hearingPart.setStatus(statusConfig);

        Session session = new Session();
        session.addHearingPart(hearingPart);
        session.setDuration(duration);
        session.setStart(sessionStartTime);

        return session;
    }

    private Session createSessionWithMultiSessionHearing() {
        Hearing hearing = new Hearing();
        hearing.setMultiSession(false);

        HearingPart hearingPart = new HearingPart();
        hearingPart.setHearing(hearing);

        Session session = new Session();
        session.addHearingPart(hearingPart);
        session.setDuration(duration);
        return session;
    }

    private void assertDoNotThrow(FailingRunnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            Assert.fail("Exception was thrown but it shouldn't. Ex: " + ex);
        }
    }
}
