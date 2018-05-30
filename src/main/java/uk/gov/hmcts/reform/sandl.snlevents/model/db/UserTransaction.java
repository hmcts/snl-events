package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTransaction {

    @Id
    @Getter
    @Setter
    private UUID id;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private UserTransactionStatus status;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private UserTransactionRulesProcessingStatus rulesProcessingStatus;

    @Getter
    @Setter
    @JsonIgnore
    private String originalData;
}
