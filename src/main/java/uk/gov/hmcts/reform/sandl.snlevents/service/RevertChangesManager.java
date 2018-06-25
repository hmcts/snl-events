package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.ws.WebServiceException;

@Service
public class RevertChangesManager {
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private FactsMapper factsMapper;

    public void revertChanges(UserTransaction ut) throws Exception {
        List<UserTransactionData> sortedUserTransactionDataList = ut.getUserTransactionDataList()
            .stream().sorted(Comparator.comparing(UserTransactionData::getCounterActionOrder))
            .collect(Collectors.toList());

        for (UserTransactionData utd : sortedUserTransactionDataList) {
            handleTransactionData(utd);
        }
    }

    public void handleTransactionData(UserTransactionData utd) throws Exception {
        if (utd.getEntity().equals("session") && utd.getCounterAction().equals("delete")) {
            Session session = sessionRepository.findOne(utd.getEntityId());

            if (session == null) {
                throw new WebServiceException("session not found");
            }

            sessionRepository.delete(utd.getEntityId());
            String msg = factsMapper.mapDbSessionToRuleJsonMessage(session);
            rulesService.postMessage(utd.getUserTransactionId(), RulesService.DELETE_SESSION, msg);
        } else if (utd.getEntity().equals("hearingPart") && utd.getCounterAction().equals("update")) {
            handleHearingPart(utd);
        }
    }

    public void handleHearingPart(UserTransactionData utd) throws Exception {
        throw new Exception("Not implemented!");
    }

}
