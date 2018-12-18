package uk.gov.hmcts.reform.sandl.snlevents.actions.session.helpers;

import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Person;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.AmendSessionRequest;
import uk.gov.hmcts.reform.sandl.snlevents.model.request.DragAndDropSessionRequest;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.function.BiConsumer;

public class AmendSessionValidators {

    private AmendSessionValidators() { }

    public static final BiConsumer<Session, DragAndDropSessionRequest> validateIfJudgeCanBeChanged =
        (Session session, DragAndDropSessionRequest dragAndDropSessionRequest) -> {
            boolean sessionHasMultiSessionHearingPart = session.getHearingParts().stream()
                .anyMatch(hp -> hp.getHearing().isMultiSession());

            boolean hasJudgeChanged = Optional.ofNullable(session.getPerson())
                .map(Person::getId)
                .filter(id -> !id.equals(dragAndDropSessionRequest.getPersonId()))
                .isPresent();

            if (sessionHasMultiSessionHearingPart && hasJudgeChanged) {
                throw new SnlRuntimeException("This session cannot be assigned to a different judge "
                    + "as it includes a multi-session hearing which needs the same judge throughout");
            }
        };

    public static final BiConsumer<Session, DragAndDropSessionRequest> validateStartAndEndDateWasShrieked =
        (Session session, DragAndDropSessionRequest dragAndDropSessionRequest) -> {
            boolean containsAtLeastOneListedHearingPart = isContainsAtLeastOneListedHearingPart(session);
            if (!containsAtLeastOneListedHearingPart) {
                return;
            }

            OffsetDateTime originalEnd = session.getStart().plus(session.getDuration());
            OffsetDateTime newEnd = dragAndDropSessionRequest.getStart().plus(
                Duration.ofSeconds(dragAndDropSessionRequest.getDurationInSeconds())
            );

            boolean startTimeMovedToRight = dragAndDropSessionRequest.getStart().isAfter(session.getStart());
            boolean endTimeMovedToLeft = newEnd.isBefore(originalEnd);

            if (startTimeMovedToRight || endTimeMovedToLeft) {
                throw new SnlRuntimeException("Session that contains at least one listed hearing part should"
                    + " start at least " + session.getStart()
                    + " and end at " + originalEnd + ".");
            }
        };

    public static final BiConsumer<Session, DragAndDropSessionRequest> canChangeDay =
        (Session session, DragAndDropSessionRequest dragAndDropSessionRequest) -> {
            boolean containsAtLeastOneListedHearingPart = isContainsAtLeastOneListedHearingPart(session);
            if (!containsAtLeastOneListedHearingPart) {
                return;
            }

            int startDay = session.getStart().getDayOfMonth();
            int endDay = session.getStart().plus(session.getDuration()).getDayOfMonth();
            int newStartDay = dragAndDropSessionRequest.getStart().getDayOfMonth();
            int newEndDay = dragAndDropSessionRequest.getStart().plus(
                Duration.ofSeconds(dragAndDropSessionRequest.getDurationInSeconds())
            ).getDayOfMonth();

            if (startDay != newStartDay || endDay != newEndDay) {
                throw new SnlRuntimeException("Cannot change start/end date of session "
                    + "that contains at least one listed hearing part.");
            }
        };


    public static final BiConsumer<Session, AmendSessionRequest> validateStartIsSetEarlier =
        (Session session, AmendSessionRequest amendSessionRequest) -> {
            boolean containsAtLeastOneListedHearingPart = isContainsAtLeastOneListedHearingPart(session);
            if (!containsAtLeastOneListedHearingPart) {
                return;
            }

            if (amendSessionRequest.getStartTime().isAfter(session.getStart())) {
                throw new SnlRuntimeException("Session that contains at least one listed hearing part should"
                    + " start at least " + session.getStart());
            }
        };

    public static final BiConsumer<Session, AmendSessionRequest> validateDurationIsGreaterOrEqual =
        (Session session, AmendSessionRequest amendSessionRequest) -> {
            boolean containsAtLeastOneListedHearingPart = isContainsAtLeastOneListedHearingPart(session);
            if (!containsAtLeastOneListedHearingPart) {
                return;
            }

            if (amendSessionRequest.getDurationInSeconds().getSeconds() < session.getDuration().getSeconds()) {
                throw new SnlRuntimeException("Duration must be greater or equal  " + session.getDuration());
            }
        };

    private static boolean isContainsAtLeastOneListedHearingPart(Session session) {
        return session.getHearingParts().stream()
            .anyMatch(hp -> hp.getStatus().getStatus() == Status.Listed);
    }

}
