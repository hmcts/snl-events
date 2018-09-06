package uk.gov.hmcts.reform.sandl.snlevents.model.rules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("squid:S3437")
public class FactCaseType implements Serializable {
    private String code;
    private String description;
}
