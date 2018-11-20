package uk.gov.hmcts.reform.sandl.snlevents.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Statusable;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.PossibleActions;

import java.util.function.BiFunction;
import java.util.function.Function;

@Data
@AllArgsConstructor
public class PossibleOperationValidator {
    private Function<Statusable, Boolean> dbConfigVerifier;

    private BiFunction<Hearing, PossibleActions, PossibleActions> businessOperationVerifier;
}
