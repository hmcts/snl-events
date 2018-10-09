package uk.gov.hmcts.reform.sandl.snlevents.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionWithHearings implements Serializable {

    private List<SessionInfo> sessions;

    @JsonProperty("hearingParts")
    private List<HearingPartResponse> hearingPartsResponse
