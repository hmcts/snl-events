package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
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

    public void revertChanges(UserTransaction ut) {
        List<UserTransactionData> sortedUserTransactionDataList = ut.getUserTransactionDataList()
            .stream().sorted(Comparator.comparing(UserTransactionData::getCounterActionOrder))
            .collect(Collectors.toList());

        for (UserTransactionData utd : sortedUserTransactionDataList) {
            handleTransactionData(utd);
        }
    }

    private void handleTransactionData(UserTransactionData utd) {
        if ("session".equals(utd.getEntity()) && "delete".equals(utd.getCounterAction())) {
            Session session = sessionRepository.findOne(utd.getEntityId());

            if (session == null) {
                throw new WebServiceException("session not found");
            }

            sessionRepository.delete(utd.getEntityId());
            String msg = factsMapper.mapDbSessionToRuleJsonMessage(session);
            try {
                rulesService.postMessage(utd.getUserTransactionId(), RulesService.DELETE_SESSION, msg);
            } catch (IOException ioex) {
                throw new SnlEventsException(ioex);
            }
        } else if ("hearingPart".equals(utd.getEntity()) && "update".equals(utd.getCounterAction())) {
            handleHearingPart(utd);
        }
    }

    @SuppressWarnings("squid:S1172") // to be removed when method below will be implemented in a  better way
    private void handleHearingPart(UserTransactionData utd) {
        throw new SnlEventsException("Not implemented!");
    }

}
