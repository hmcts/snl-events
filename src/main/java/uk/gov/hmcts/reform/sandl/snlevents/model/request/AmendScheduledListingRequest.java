package uk.gov.hmcts.reform.sandl.snlevents.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmendScheduledListingRequest {
    private UUID userTransactionId;
    private UUID hearingPartId;
    private Long hearingPartVersion;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
}
