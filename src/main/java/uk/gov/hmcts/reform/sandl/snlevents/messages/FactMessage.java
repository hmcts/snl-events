package uk.gov.hmcts.reform.sandl.snlevents.messages;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FactMessage {
    private String type;
    private String data;
}
