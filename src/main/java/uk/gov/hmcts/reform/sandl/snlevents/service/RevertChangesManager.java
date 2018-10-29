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

import javax.persistence.EntityManager;

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
    EntityManager entityManager;

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
            Hearing previousHearing;

            try {
                previousHearing = objectMapper.readValue(utd.getBeforeData(), Hearing.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            previousHearing.getHearingParts().stream()
                .forEach(hp -> {
                    String msg;
                    try {
                        // For some reason after serialization haring is null event thought hearingId is set
                        hp.setHearing(hearing);
                        msg = factsMapper.mapHearingToRuleJsonMessage(hp);
                        rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });

            entityManager.detach(hearing);

            previousHearing.setVersion(hearing.getVersion());
            entityManager.merge(previousHearing);

            previousHearing.getHearingParts().get(0).setHearing(hearingRepository.findOne(previousHearing.getId()));

            hearingRepository.save(previousHearing);
        } else if ("delete".equals(utd.getCounterAction())) {
            hearingRepository.delete(utd.getEntityId());
        }
    }

    private void handleHearingPart(UserTransactionData utd) {
        HearingPart hp = hearingPartRepository.findById(utd.getEntityId());

        if ("update".equals(utd.getCounterAction())) {
            HearingPart previousHearingPart = new HearingPart();
            String msg = null;

            try {
                previousHearingPart = objectMapper.readValue(utd.getBeforeData(), HearingPart.class);
                previousHearingPart.setHearing(hearingRepository.findOne(previousHearingPart.getHearingId()));

                msg = factsMapper.mapHearingToRuleJsonMessage(previousHearingPart);
                //to have both entities
                //hearing and hearingPart then we would have to send a hearingPart here
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);

            entityManager.detach(previousHearingPart);

            previousHearingPart.setVersion(hp.getVersion());
            entityManager.merge(previousHearingPart);

            if (previousHearingPart.getSessionId() != null) {
                previousHearingPart.setSession(sessionRepository.findOne(previousHearingPart.getSessionId()));

            }

            hearingPartRepository.save(previousHearingPart);
        } else if ("delete".equals(utd.getCounterAction())) {
            hearingPartRepository.delete(utd.getEntityId());

            String msg = null;
            try {
                msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            rulesService.postMessage(utd.getUserTransactionId(), RulesService.DELETE_HEARING_PART, msg);
        }
    }

    @SuppressWarnings("squid:S1172") // to be removed when method below will be implemented in a  better way
//    private void handleHearingPart(UserTransactionData utd) {
//        HearingPart hp = hearingPartRepository.findById(utd.getEntityId());
//
//        if ("update".equals(utd.getCounterAction())) {
//            HearingPart previousHearingPart = new HearingPart();
//            String msg = null;
//
//            try {
//                previousHearingPart = objectMapper.readValue(utd.getBeforeData(), HearingPart.class);
//
//                //msg = factsMapper.mapDbHearingToRuleJsonMessage(
//                //hearingRepository.findOne(previousHearingPart.getHearingId())); @TODO if we change the rules
//                //to have both entities
//                //hearing and hearingPart then we would have to send a hearingPart here
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            //rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);
//
//            entityManager.detach(previousHearingPart);
//
//            previousHearingPart.setVersion(hp.getVersion());
//            entityManager.merge(previousHearingPart);
//
//            previousHearingPart.setHearing(hearingRepository.findOne(previousHearingPart.getHearingId()));
//            if (previousHearingPart.getSessionId() != null) {
//                previousHearingPart.setSession(sessionRepository.findOne(previousHearingPart.getSessionId()));
//
//            }
//
//            hearingPartRepository.save(previousHearingPart);
//        } else if ("delete".equals(utd.getCounterAction())) {
//            hearingPartRepository.delete(utd.getEntityId());
//
//            String msg = null;
//            try {
//                msg = factsMapper.mapHearingToRuleJsonMessage(hp);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            rulesService.postMessage(utd.getUserTransactionId(), RulesService.DELETE_HEARING_PART, msg);
//        }
//    }

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
