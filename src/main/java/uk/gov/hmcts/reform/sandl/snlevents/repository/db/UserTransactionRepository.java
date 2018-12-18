package uk.gov.hmcts.reform.sandl.snlevents.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction.UserTransactionStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserTransactionRepository extends JpaRepository<UserTransaction, UUID> {
    List<UserTransaction> getAllByStartedAtBeforeAndStatusNotInOrderByStartedAtAsc(
        OffsetDateTime start, UserTransactionStatus[] status);
}
