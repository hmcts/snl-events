package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VersionInfo {
    private UUID id;
    private Long version;
}
