package uk.gov.hmcts.reform.sandl.snlevents.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionRulesProcessingStatus;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserTransaction {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    private UserTransactionStatus status;

    @Enumerated(EnumType.STRING)
    private UserTransactionRulesProcessingStatus rulesProcessingStatus;

    @OneToMany(mappedBy = "userTransaction", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<UserTransactionData> userTransactionDataList = new ArrayList<>();

    //IN FUTURE add here also version, optimistic locking
    public UserTransaction(UUID id, UserTransactionStatus status,
                           UserTransactionRulesProcessingStatus rulesProcessingStatus) {
        this.id = id;
        this.status = status;
        this.rulesProcessingStatus = rulesProcessingStatus;
    }

    public void addUserTransactionData(UserTransactionData userTransactionData) {
        userTransactionDataList.add(userTransactionData);
        userTransactionData.setUserTransaction(this);
    }

    public void addUserTransactionData(List<UserTransactionData> userTransactionDataList) {
        for (UserTransactionData utd : userTransactionDataList) {
            this.addUserTransactionData(utd);
        }
    }

    public void removeUserTransactionData(UserTransactionData userTransactionData) {
        userTransactionDataList.remove(userTransactionData);
        userTransactionData.setUserTransaction(null);
    }
}
