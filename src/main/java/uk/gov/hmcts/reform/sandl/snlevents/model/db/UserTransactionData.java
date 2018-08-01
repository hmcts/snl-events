package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTransactionData {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String entity;
    private UUID entityId;
    private String beforeData;
    private String action;
    private String counterAction;
    private int counterActionOrder;

    @ManyToOne
    @JsonIgnore
    private UserTransaction userTransaction;

    @Column(name = "user_transaction_id", updatable = false, insertable = false)
    @JsonProperty("userTransaction")
    private UUID userTransactionId;

    public UserTransactionData(String entity,
                               UUID entityId,
                               String beforeData,
                               String action,
                               String counterAction,
                               int counterActionOrder) {
        this.entity = entity;
        this.entityId = entityId;
        this.beforeData = beforeData;
        this.action = action;
        this.counterAction = counterAction;
        this.counterActionOrder = counterActionOrder;
    }
}
