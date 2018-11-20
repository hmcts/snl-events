package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.PossibleOperationValidator;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Statusable;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.PossibleActions;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

@Service
public class StatusServiceManager {

    private List<PossibleOperationValidator> possibleOperationConfig = new ArrayList();

    {
        possibleOperationConfig.add(new PossibleOperationValidator(checkIfStatusCanBeAdjourned,
            checkIfAdjournCanBePerformed));
        possibleOperationConfig.add(new PossibleOperationValidator(checkIfStatusCanBeWithdrawn,
            checkIfWithdrawCanBePerformed));
    }

    public boolean canBeListed(Statusable entity) {
        return entity.getStatus().isCanBeListed();
    }

    public boolean canBeUnlisted(Statusable entity) {
        return entity.getStatus().isCanBeUnlisted();
    }

    public boolean shouldBeCountInUtilization(Statusable entity) {
        return entity.getStatus().isCountInUtilization();
    }

    public PossibleActions getPossibleActions(HearingWithSessionsResponse hearing) {
        PossibleActions possibleActions = new PossibleActions();

        possibleOperationConfig.stream()
            .filter(validator -> validator.getDbConfigVerifier().apply(hearing))
            .forEach(validator -> validator.getBusinessOperationVerifier().apply(hearing, possibleActions));

        return possibleActions;
    }

    private static Function<HearingWithSessionsResponse, Boolean> checkIfStatusCanBeAdjourned =
        (HearingWithSessionsResponse entity) -> entity.getStatusConfig().isCanBeAdjourned();

    private static Function<HearingWithSessionsResponse, Boolean> checkIfStatusCanBeWithdrawn =
        (HearingWithSessionsResponse entity) -> entity.getStatusConfig().isCanBeWithdrawn();


    private static BiFunction<HearingWithSessionsResponse, PossibleActions, PossibleActions>
        checkIfAdjournCanBePerformed =
            (HearingWithSessionsResponse hearing, PossibleActions possibleActions) -> {
                // @TODO: implement logic
                return possibleActions;
            };

    private static BiFunction<HearingWithSessionsResponse, PossibleActions, PossibleActions>
        checkIfWithdrawCanBePerformed =
            (HearingWithSessionsResponse hearing, PossibleActions possibleActions) -> {
                // @TODO: implement logic
                return possibleActions;
            };
}
