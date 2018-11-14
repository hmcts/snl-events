package uk.gov.hmcts.reform.sandl.snlevents.model.rules;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RulesEngineReloadStatus {
    private String name;
    private FactReloadStatus reloadStatus;
}
