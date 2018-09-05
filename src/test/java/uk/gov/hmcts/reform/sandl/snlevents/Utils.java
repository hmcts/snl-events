package uk.gov.hmcts.reform.sandl.snlevents;

import lombok.val;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.CaseTypeWithHearingTypesResponse;
import uk.gov.hmcts.reform.sandl.snlevents.model.response.SimpleDictionaryData;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {

    private Utils() {}

    public static List<CaseTypeWithHearingTypesResponse>
        getCaseTypeWithHearingTypesResponses(String defaultCode, String defaultDescription) {
        val ct = new CaseTypeWithHearingTypesResponse();
        Set<SimpleDictionaryData> hts =
            Stream.of(new SimpleDictionaryData(defaultCode, defaultDescription))
                .collect(Collectors.toSet());

        ct.setCode(defaultCode);
        ct.setDescription(defaultDescription);
        ct.setHearingTypes(hts);
        return Arrays.asList(ct);
    }
}
