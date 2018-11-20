package uk.gov.hmcts.reform.sandl.snlevents.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.function.BiFunction;
import java.util.function.Function;

@Data
@AllArgsConstructor
public class PossibleOperationValidator {
    private Function dbConfigVerifier;

    private BiFunction businessOperationVerifier;
}
