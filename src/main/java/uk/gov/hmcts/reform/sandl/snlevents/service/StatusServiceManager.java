package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.PossibleOperationValidator;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Statusable;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.PossibleActions;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

@Service
public class StatusServiceManager {

    private List<PossibleOperationValidator> possibleOperationConfig = new ArrayList();

    public StatusServiceManager() {
        possibleOperationConfig.add(new PossibleOperationValidator(checkIfStatusCanBeAdjourned,
            checkIfAdjournCanBePerformed));
        possibleOperationConfig.add(new PossibleOperationValidator(checkIfStatusCanBeWithdrawn,
            checkIfWithdrawCanBePerformed));
        possibleOperationConfig.add(new PossibleOperationValidator(checkIfStatusCanBeUnlisted,
            checkIfUnlistCanBePerformed));
        possibleOperationConfig.add(new PossibleOperationValidator(checkIfStatusCanBeVacated,
            checkIfVacatedCanBePerformed));
    }

    public boolean canBeListed(Statusable entity) {
        return entity.getStatus().isCanBeListed();
    }

    public boolean canBeUnlisted(Statusable entity) {
        return entity.getStatus().isCanBeUnlisted();
    }

    public boolean canHearingPartBeAdjourned(HearingPart entity) {
        return entity.getStatus().isCanBeAdjourned();
    }

    public boolean shouldBeCountInUtilization(Statusable entity) {
        return entity.getStatus().isCountInUtilization();
    }

    public PossibleActions getPossibleActions(HearingWithSessionsResponse hearing) {
        PossibleActions possibleActions = new PossibleActions();

        possibleOperationConfig.stream()
            .filter(validator -> validator.getDbConfigVerifier().test(hearing))
            .forEach(validator -> validator.getBusinessOperationVerifier().apply(hearing, possibleActions));

        return possibleActions;
    }

    public boolean canBeWithdrawn(HearingPart hearingPart) {
        return hearingPart.getStatus().isCanBeWithdrawn();
    }

    public boolean canBeWithdrawn(Hearing hearing) {
        HearingWithSessionsResponse hearingWithSessionsResponse = new HearingWithSessionsResponse(hearing);

        return checkIfStatusCanBeWithdrawn.test(hearingWithSessionsResponse)
            && checkIfWithdrawCanBePerformed(hearingWithSessionsResponse);
    }

    public boolean canBeAdjourned(Hearing hearing) {
        HearingWithSessionsResponse hearingWithSessionsResponse = new HearingWithSessionsResponse(hearing);

        return checkIfStatusCanBeAdjourned.test(hearingWithSessionsResponse)
            && checkIfAdjournCanBePerformed(hearingWithSessionsResponse);
    }

    public boolean canBeVacated(Hearing hearing) {
        HearingWithSessionsResponse hearingWithSessionsResponse = new HearingWithSessionsResponse(hearing);

        return checkIfStatusCanBeVacated.test(hearingWithSessionsResponse)
            && checkIfVacatedCanBePerformed(hearingWithSessionsResponse);
    }

    public boolean canBeVacated(HearingPart hearingPart) {
        return hearingPart.getStatus().getStatus() == Status.Listed
            && hearingPart.getStart().isAfter(OffsetDateTime.now());
    }

    private static Predicate<HearingWithSessionsResponse> checkIfStatusCanBeUnlisted =
        (HearingWithSessionsResponse entity) -> entity.getStatusConfig().isCanBeUnlisted();

    private static Predicate<HearingWithSessionsResponse> checkIfStatusCanBeAdjourned =
        (HearingWithSessionsResponse entity) -> entity.getStatusConfig().isCanBeAdjourned();

    private static Predicate<HearingWithSessionsResponse> checkIfStatusCanBeWithdrawn =
        (HearingWithSessionsResponse entity) -> entity.getStatusConfig().isCanBeWithdrawn();

    private static Predicate<HearingWithSessionsResponse> checkIfStatusCanBeVacated =
        (HearingWithSessionsResponse entity) -> entity.getStatusConfig().isCanBeVacated();

    private static BiFunction<HearingWithSessionsResponse, PossibleActions, PossibleActions>
        checkIfAdjournCanBePerformed =
            (HearingWithSessionsResponse hearing, PossibleActions possibleActions) -> {
                possibleActions.setAdjourn(checkIfAdjournCanBePerformed(hearing));
                return possibleActions;
            };

    private static boolean checkIfAdjournCanBePerformed(HearingWithSessionsResponse hearingWithSessionsResponse) {
        OffsetDateTime listingDate = hearingWithSessionsResponse.getListingDate();
        return listingDate == null ? false : listingDate.isBefore(OffsetDateTime.now()); //NOSONAR
    }

    private static BiFunction<HearingWithSessionsResponse, PossibleActions, PossibleActions>
        checkIfWithdrawCanBePerformed =
            (HearingWithSessionsResponse hearing, PossibleActions possibleActions) -> {
                possibleActions.setWithdraw(checkIfWithdrawCanBePerformed(hearing));
                return possibleActions;
            };

    private static boolean checkIfWithdrawCanBePerformed(HearingWithSessionsResponse hearingWithSessionsResponse) {
        return hearingWithSessionsResponse.getListingDate() == null
            || !hearingWithSessionsResponse.getListingDate().toLocalDate().isBefore(
                OffsetDateTime.now().toLocalDate());
    }

    private static BiFunction<HearingWithSessionsResponse, PossibleActions, PossibleActions>
        checkIfUnlistCanBePerformed = (HearingWithSessionsResponse hearing, PossibleActions possibleActions) -> {
            possibleActions.setUnlist(true);
            return possibleActions;
        };

    private static boolean checkIfVacatedCanBePerformed(HearingWithSessionsResponse hearingWithSessionsResponse) {
        return hearingWithSessionsResponse.getStatus() == Status.Listed
            && hearingWithSessionsResponse.isMultiSession()
            && hearingWithSessionsResponse.getListingDate().isBefore(OffsetDateTime.now());
    }

    private static BiFunction<HearingWithSessionsResponse, PossibleActions, PossibleActions>
        checkIfVacatedCanBePerformed = (HearingWithSessionsResponse hearing, PossibleActions possibleActions) -> {
            possibleActions.setVacate(checkIfVacatedCanBePerformed(hearing));
            return possibleActions;
        };
}
