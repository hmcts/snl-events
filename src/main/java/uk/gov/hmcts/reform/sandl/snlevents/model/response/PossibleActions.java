package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PossibleActions {
    @JsonProperty(value = "Unlist")
    private boolean unlist;

    @JsonProperty(value = "Withdraw")
    private boolean withdraw;

    @JsonProperty(value = "Adjourn")
    private boolean adjourn;

    @JsonProperty(value = "Vacate")
    private boolean vacate;
}
