package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.Data;

@Data
public class PossibleActions {

    private boolean unlist;

    private boolean withdraw;

    private boolean adjourn;
}
