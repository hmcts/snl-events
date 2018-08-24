package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlEventsException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
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
    private HearingPartRepository hearingPartRepository;

    @Autowired
    private RulesService rulesService;

    @Autowired
    private FactsMapper factsMapper;

    @Autowired
    ObjectMapper objectMapper;

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
        } else if ("hearingPart".equals(utd.getEntity())) {
            handleHearingPart(utd);
        }
    }

    @SuppressWarnings("squid:S1172") // to be removed when method below will be implemented in a  better way
    private void handleHearingPart(UserTransactionData utd) {
        if("update".equals(utd.getCounterAction())) {
            HearingPart hp = hearingPartRepository.findOne(utd.getEntityId());

            HearingPart previousHearingPart;

            try {
                previousHearingPart = objectMapper.readValue(utd.getBeforeData(), HearingPart.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            previousHearingPart.setVersion(hp.getVersion());

            hearingPartRepository.save(previousHearingPart);
        } else if("delete".equals(utd.getCounterAction())) {

        }
    }
}
