package uk.gov.hmcts.reform.sandl.snlevents.model.rules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("squid:S3437")
public class FactSessionType implements Serializable {
    private String id;
    private List<FactCaseType> caseTypes;
    private List<FactHearingType> hearingTypes;
}
