package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.sandl.snlevents.interfaces.SimpleDictonarySettable;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@EqualsAndHashCode
@NoArgsConstructor
public abstract class BaseReferenceData implements SimpleDictonarySettable {
    @Id
    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String description;

    public BaseReferenceData(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
