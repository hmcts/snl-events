package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.PossibleOperationValidator;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Statusable;
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

    public PossibleActions getPossibleActions(Hearing hearing) {
        PossibleActions possibleActions = new PossibleActions();

        possibleOperationConfig.stream()
            .filter(validator -> (Boolean) validator.getDbConfigVerifier().apply(hearing))
            .forEach(validator -> validator.getBusinessOperationVerifier().apply(hearing, possibleActions));

        return possibleActions;
    }

    private static Function<Statusable, Boolean> checkIfStatusCanBeAdjourned = (Statusable entity) ->
        entity.getStatus().isCanBeAdjourned();

    private static Function<Statusable, Boolean> checkIfStatusCanBeWithdrawn = (Statusable entity) ->
        entity.getStatus().isCanBeWithdrawn();


    private static BiFunction<Hearing, PossibleActions, PossibleActions> checkIfAdjournCanBePerformed =
        (Hearing hearing, PossibleActions possibleActions) -> {
            // @TODO: implement logic
            return possibleActions;
        };

    private static BiFunction<Hearing, PossibleActions, PossibleActions> checkIfWithdrawCanBePerformed =
        (Hearing hearing, PossibleActions possibleActions) -> {
            // @TODO: implement logic
            return possibleActions;
        };
}
