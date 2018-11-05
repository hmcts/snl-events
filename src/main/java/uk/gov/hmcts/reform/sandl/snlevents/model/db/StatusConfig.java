package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class StatusConfig implements Serializable {

    @Id
    private String status;

    private boolean canBeListed;

    private boolean canBeUnlisted;

    private boolean countInUtilization;
}
