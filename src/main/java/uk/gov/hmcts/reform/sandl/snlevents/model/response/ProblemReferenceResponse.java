package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemReferenceResponse {
    private String id;
    private String entity;
    @JsonProperty("entity_id")
    private String entityId;
    private String description;
    @JsonProperty("problem_id")
    private String problemId;
}
