package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("squid:S3437")
public class SessionAmendResponse implements Serializable {

    UUID id;

    OffsetDateTime start;

    Duration duration;

    String sessionTypeCode;

    String personName;

    String roomName;

    String roomDescription;

    String roomTypeCode;

    int hearingPartsCount;

    boolean hasMultiSessionHearingAssigned;

    Long version;

    boolean hasListedHearingParts;
}
