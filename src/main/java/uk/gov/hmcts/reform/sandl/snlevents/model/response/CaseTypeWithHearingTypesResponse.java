package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingType;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CaseTypeWithHearingTypesResponse extends SimpleDictionaryData implements Serializable {
    private Set<SimpleDictionaryData> hearingTypes;
}
