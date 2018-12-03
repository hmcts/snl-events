package uk.gov.hmcts.reform.sandl.snlevents.model.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class SessionWithHearingPartsFacts {
    private String sessionFact;
    private List<String> hearingPartsFacts;
}
