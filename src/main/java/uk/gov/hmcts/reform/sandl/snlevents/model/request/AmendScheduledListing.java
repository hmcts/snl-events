package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.validation.annotations.TimeFormat;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmendScheduledListing {
    private UUID userTransactionId;
    private UUID hearingPartId;
    private Long hearingPartVersion;

    @TimeFormat(timeFormat = "HH:mm")
    private String startTime;
}
