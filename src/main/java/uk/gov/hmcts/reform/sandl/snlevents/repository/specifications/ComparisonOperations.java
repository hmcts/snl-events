package uk.gov.hmcts.reform.sandl.snlevents.repository.specifications;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ComparisonOperations {

    @JsonProperty("equals")
    EQUALS,

    @JsonProperty("in")
    IN,

    @JsonProperty("in or null")
    IN_OR_NULL,

    @JsonProperty("like")
    LIKE,

}
