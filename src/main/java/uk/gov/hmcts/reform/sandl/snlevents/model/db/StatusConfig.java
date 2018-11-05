package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class StatusConfig implements Serializable {

    @Id
    @Getter
    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean canBeListed;

    private boolean canBeUnlisted;

    private boolean countInUtilization;
}
