package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;

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
