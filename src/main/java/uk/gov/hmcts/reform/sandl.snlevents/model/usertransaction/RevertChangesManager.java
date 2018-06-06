package uk.gov.hmcts.reform.sandl.snlevents.model.usertransaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class RevertChangesManager {
    @Autowired
    private SessionRepository sessionRepository;

    public void revertChanges(UserTransaction ut) {
        for (UserTransactionData utd :ut.getUserTransactionDataList()
                .stream().sorted(Comparator.comparing(UserTransactionData::getCounterActionOrder))
                .collect(Collectors.toList())) {
            if (utd.getEntity().equals("session") && utd.getCounterAction().equals("delete")) {
                sessionRepository.delete(utd.getEntityId());
            }
        }
    }
}
