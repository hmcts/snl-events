package uk.gov.hmcts.reform.sandl.snlevents.model.request.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactional;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.MinDuration;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HearingSearchCriteria {
    private String caseNumber;
    private String caseTitle;
    private List<String> priorities;
    private List<String> caseTypes;
    private List<String> hearingTypes;
    private List<String> communicationFacilitators;
    private List<String> judges;
    private String listingDetails;
}
