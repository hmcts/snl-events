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
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.ActivityLogRepository;
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
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    public void revertChanges(UserTransaction ut) {
        List<UserTransactionData> sortedUserTransactionDataList = ut.getUserTransactionDataList()
            .stream().sorted(Comparator.comparing(UserTransactionData::getCounterActionOrder))
            .collect(Collectors.toList());

        for (UserTransactionData utd : sortedUserTransactionDataList) {
            handleTransactionData(utd);
        }
        revertActivityLogData(ut);
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

    private void revertActivityLogData(UserTransaction userTransaction) {
        activityLogRepository.deleteActivityLogByUserTransactionId(userTransaction.getId());
    }

    private void handleHearing(UserTransactionData utd) {

        if ("update".equals(utd.getCounterAction())) {
            Hearing hearing = hearingRepository.findOne(utd.getEntityId());
            Hearing previousHearing;

            try {
                previousHearing = objectMapper.readValue(utd.getBeforeData(), Hearing.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            previousHearing.getHearingParts().stream().forEach(hp -> {
                // For some reason after serialization hearing is null even though hearingId is set
                hp.setHearing(hearing);
                String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
                rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);
            });

            entityManager.detach(hearing);

            previousHearing.setVersion(hearing.getVersion());
            entityManager.merge(previousHearing);

            previousHearing.getHearingParts().get(0).setHearing(hearingRepository.findOne(previousHearing.getId()));

            hearingRepository.save(previousHearing);
        } else if ("delete".equals(utd.getCounterAction())) {
            hearingRepository.delete(utd.getEntityId());
        } else if ("create".equals(utd.getCounterAction())) {
            Hearing deletedHearing = hearingRepository.getHearingByIdIgnoringWhereDeletedClause(utd.getEntityId());
            deletedHearing.setDeleted(false);

            List<HearingPart> hearingParts = hearingPartRepository
                .getHearingPartsByHearingIdIgnoringWhereDeletedClause(deletedHearing.getId());
            hearingParts.forEach(hp -> hp.setDeleted(false));

            hearingPartRepository.save(hearingParts);
            hearingRepository.save(deletedHearing);

            hearingParts.forEach(hp -> {
                String msg = factsMapper.mapHearingPartToRuleJsonMessage(hp);
                rulesService.postMessage(utd.getUserTransactionId(), RulesService.UPSERT_HEARING_PART, msg);
            });
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

            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
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
            SessionWithHearingPartsFacts sessionWithHpFacts = factsMapper.mapDbSessionToRuleJsonMessage(
                session,
                session.getHearingParts()
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

            Session session = sessionRepository.findOne(utd.getEntityId());
            previousSession.setVersion(session.getVersion());

            sessionRepository.save(previousSession);
        }
    }
}
