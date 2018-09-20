package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateProblem {
    private String id;
    private String type;
    private String severity;
    private String message;
    private List<CreateProblemReference> references;
    private OffsetDateTime createdAt;
}
