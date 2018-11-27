package uk.gov.hmcts.reform.sandl.snlevents.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.HearingWithSessionsResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.PossibleActions;

import java.util.function.BiFunction;
import java.util.function.Predicate;

@Data
@AllArgsConstructor
public class PossibleOperationValidator {
    private Predicate<HearingWithSessionsResponse> dbConfigVerifier;

    private BiFunction<HearingWithSessionsResponse, PossibleActions, PossibleActions> businessOperationVerifier;
}
