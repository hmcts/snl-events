package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserTransactionDataRepository extends JpaRepository<UserTransactionData, UUID> {
    public List<UserTransactionData> findByEntityIdEqualsAndUserTransaction_StatusEquals(UUID id,
                                                                                         UserTransactionStatus status);
}
