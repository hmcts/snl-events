package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.Status;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class ActivityLog {
    @Id
    private UUID id;

    private UUID userTransactionId;

    private UUID entityId;

    private String entityName;

    @Enumerated(EnumType.STRING)
    private Status status;

    private String description;
}
