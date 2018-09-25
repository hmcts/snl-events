package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemResponse {
    private String id;
    private String type;
    private String message;
    private String severity;
    private List<ProblemReferenceResponse> references;
    private OffsetDateTime createdAt;
}
