package uk.gov.hmcts.reform.sandl.snlevents.model.request.search;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ComparisonOperations {

    @JsonProperty("equals")
    EQUALS,

    @JsonProperty("in")
    IN,

    @JsonProperty("like")
    LIKE
}
