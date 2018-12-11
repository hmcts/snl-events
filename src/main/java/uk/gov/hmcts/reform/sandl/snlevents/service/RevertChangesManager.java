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
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.SessionWithHearingPartsFacts;
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
        if ("update".equals(utd.getCounterAction())) {
            Hearing hearingToRollback = hearingRepository.findOne(utd.getEntityId());
            Hearing previousHearing;

            try {
                previousHearing = objectMapper.readValue(utd.getBeforeData(), Hearing.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            entityManager.detach(previousHearing);
            previousHearing.setVersion(hearingToRollback.getVersion());
            entityManager.merge(previousHearing);
        } else if ("delete".equals(utd.getCounterAction())) {
            Hearing hearingToRollback = hearingRepository.findOne(utd.getEntityId());

            hearingRepository.delete(hearingToRollback);
        } else if ("create".equals(utd.getCounterAction())) {
            Hearing hearingToRollback = hearingRepository.getHearingByIdIgnoringWhereDeletedClause(utd.getEntityId());

            entityManager.detach(hearingToRollback);
            hearingToRollback.setDeleted(false);
            entityManager.merge(hearingToRollback);
        }
    }

    private void handleHearingPart(UserTransactionData utd) {
        HearingPart hearingPartToRollback = hearingPartRepository.findOne(utd.getEntityId());

        if ("update".equals(utd.getCounterAction())) {
            HearingPart previousHearingPart;

            try {
                previousHearingPart = objectMapper.readValue(utd.getBeforeData(), HearingPart.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            entityManager.detach(previousHearingPart);
            previousHearingPart.setVersion(hearingPartToRollback.getVersion());

            if (previousHearingPart.getHearingId() != null) {
                previousHearingPart.setHearing(hearingRepository.findOne(previousHearingPart.getHearingId()));
            }

            if (previousHearingPart.getSessionId() != null) {
                previousHearingPart.setSession(sessionRepository.findOne(previousHearingPart.getSessionId()));
            }

            String msg = factsMapper.mapHearingToRuleJsonMessage(previousHearingPart);
            rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);

            entityManager.merge(previousHearingPart);
        } else if ("delete".equals(utd.getCounterAction())) {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hearingPartToRollback);
            rulesService.postMessage(utd.getUserTransactionId(), RulesService.DELETE_HEARING_PART, msg);

            hearingPartRepository.delete(hearingPartToRollback);
        } else if ("create".equals(utd.getCounterAction())) {
            entityManager.detach(hearingPartToRollback);
            hearingPartToRollback.setDeleted(false);

            String msg = factsMapper.mapHearingToRuleJsonMessage(hearingPartToRollback);
            rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);

            entityManager.merge(hearingPartToRollback);
        }
    }

    private void handleSession(UserTransactionData utd) {
        Session sessionToRollback = sessionRepository.findOne(utd.getEntityId());

        if ("delete".equals(utd.getCounterAction())) {
            if (sessionToRollback == null) {
                throw new EntityNotFoundException("session not found");
            }

            sessionRepository.delete(sessionToRollback);
            SessionWithHearingPartsFacts sessionWithHpFacts = factsMapper.mapDbSessionToRuleJsonMessage(
                sessionToRollback,
                sessionToRollback.getHearingParts()
            );
            rulesService.postMessage(
                utd.getUserTransactionId(),
                RulesService.DELETE_SESSION,
                sessionWithHpFacts.getSessionFact()
            );
            sessionWithHpFacts.getHearingPartsFacts().forEach(hpFact ->
                rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, hpFact)
            );

        } else if ("update".equals(utd.getCounterAction())) {
            Session previousSession;

            try {
                previousSession = objectMapper.readValue(utd.getBeforeData(), Session.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            SessionWithHearingPartsFacts sessionWithHpFacts =
                factsMapper.mapDbSessionToRuleJsonMessage(previousSession, previousSession.getHearingParts());

            rulesService.postMessage(
                utd.getUserTransactionId(),
                RulesService.UPSERT_SESSION,
                sessionWithHpFacts.getSessionFact()
            );

            sessionWithHpFacts.getHearingPartsFacts().forEach(hpFact ->
                rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, hpFact)
            );

            entityManager.detach(previousSession);
            previousSession.setVersion(sessionToRollback.getVersion());
            entityManager.merge(previousSession);
        }
    }
}
