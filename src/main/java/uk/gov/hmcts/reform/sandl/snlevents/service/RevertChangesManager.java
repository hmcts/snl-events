package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.EntityNotFoundException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransaction;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RevertChangesManager {
    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private HearingPartRepository hearingPartRepository;

    @Autowired
    private HearingRepository hearingRepository;

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
        if ("session".equals(utd.getEntity())) {
            handleSession(utd);
        } else if ("hearingPart".equals(utd.getEntity())) {
            handleHearingPart(utd);
        } else if ("hearing".equals(utd.getEntity())) {
            handleHearing(utd);
        }
    }

    private void handleHearing(UserTransactionData utd) {
        Hearing hearing = hearingRepository.findOne(utd.getEntityId());

        if ("update".equals(utd.getCounterAction())) {
            Hearing previousHearing = new Hearing();
            String msg = null;

            try {
                previousHearing = objectMapper.readValue(utd.getBeforeData(), Hearing.class);
                msg = factsMapper.mapDbHearingToRuleJsonMessage(previousHearing);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);

            previousHearing.setVersion(hearing.getVersion());

            hearingRepository.save(previousHearing);
        } else if ("delete".equals(utd.getCounterAction())) {
            hearingRepository.delete(utd.getEntityId());
        }

    }

    @SuppressWarnings("squid:S1172") // to be removed when method below will be implemented in a  better way
    private void handleHearingPart(UserTransactionData utd) {
        HearingPart hp = hearingPartRepository.findOne(utd.getEntityId());

        if ("update".equals(utd.getCounterAction())) {
            HearingPart previousHearingPart = new HearingPart();
            String msg = null;

            try {
                previousHearingPart = objectMapper.readValue(utd.getBeforeData(), HearingPart.class);
                msg = factsMapper.mapDbHearingToRuleJsonMessage(previousHearingPart.getHearing());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);

            previousHearingPart.setVersion(hp.getVersion());

            hearingPartRepository.save(previousHearingPart);
        } else if ("delete".equals(utd.getCounterAction())) {
            hearingPartRepository.delete(utd.getEntityId());

            String msg = null;
            try {
                msg = factsMapper.mapDbHearingToRuleJsonMessage(hp.getHearing());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            rulesService.postMessage(utd.getUserTransactionId(), RulesService.DELETE_HEARING_PART, msg);
        }
    }

    private void handleSession(UserTransactionData utd) {
        if ("delete".equals(utd.getCounterAction())) {
            Session session = sessionRepository.findOne(utd.getEntityId());

            if (session == null) {
                throw new EntityNotFoundException("session not found");
            }

            sessionRepository.delete(utd.getEntityId());
            String msg = factsMapper.mapDbSessionToRuleJsonMessage(session);
            rulesService.postMessage(utd.getUserTransactionId(), RulesService.DELETE_SESSION, msg);

        } else if ("update".equals(utd.getCounterAction())) {
            Session session = sessionRepository.findOne(utd.getEntityId());

            Session previousSession;
            String msg;

            try {
                previousSession = objectMapper.readValue(utd.getBeforeData(), Session.class);
                msg = factsMapper.mapDbSessionToRuleJsonMessage(previousSession);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_SESSION, msg);

            previousSession.setVersion(session.getVersion());

            sessionRepository.save(previousSession);

        }
    }
}
