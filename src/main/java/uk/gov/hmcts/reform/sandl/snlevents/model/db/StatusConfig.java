package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
@Builder
@Data
public class StatusConfig implements Serializable {

    @Id
    @Enumerated(EnumType.STRING)
    private Status status;

    private boolean canBeListed;

    private boolean canBeUnlisted;

    private boolean countInUtilization;

    private boolean canBeVacated;

    private boolean canBeAdjourned;

    private boolean canBeWithdrawn;
}
