package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserTransaction implements Serializable {

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
    @JsonIgnore
    private String originalData;
}
