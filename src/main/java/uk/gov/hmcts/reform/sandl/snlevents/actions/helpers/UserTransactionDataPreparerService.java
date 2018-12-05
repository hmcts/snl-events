package uk.gov.hmcts.reform.sandl.snlevents.actions.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;
import uk.gov.hmcts.reform.sandl.snlevents.service.RulesService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserTransactionDataPreparerService {
    private List<UserTransactionData> userTransactionDataList = new ArrayList<>();

    public List<UserTransactionData> getUserTransactionDataList() {
        return userTransactionDataList;
    }

    public void prepareUserTransactionDataForCreate(String entity, UUID entityId, int counterActionOrder) {
        userTransactionDataList.add(new UserTransactionData(
            entity,
            entityId,
            null,
            "create",
            "delete",
            counterActionOrder)
        );
    }

    public void prepareUserTransactionDataForDelete(String entity, UUID entityId, String previousEntityString,
                                                    int counterActionOrder) {
        userTransactionDataList.add(new UserTransactionData(
            entity,
            entityId,
            previousEntityString,
            "delete",
            "create",
            counterActionOrder)
        );
    }

    public void prepareUserTransactionDataForUpdate(String entity, UUID entityId, String previousEntityString,
                                                    int counterActionOrder) {
        userTransactionDataList.add(new UserTransactionData(
            entity,
            entityId,
            previousEntityString,
            "update",
            "update",
            counterActionOrder)
        );
    }

    public void prepareLockedEntityTransactionData(String entity, UUID entityId, int counterActionOrder) {
        userTransactionDataList.add(new UserTransactionData(
            entity,
            entityId,
            null,
            "lock",
            "unlock",
            counterActionOrder)
        );
    }

    public Map<UUID, String> mapHearingPartsToStrings(ObjectMapper objectMapper, List<HearingPart> hearingParts) {
        Map<UUID, String> originalIdStringPair = new HashMap<>();
        hearingParts.forEach(hp -> {
            try {
                String hearingPartString = objectMapper.writeValueAsString(hp);
                originalIdStringPair.put(hp.getId(), hearingPartString);
            } catch (JsonProcessingException e) {
                throw new SnlRuntimeException(e);
            }
        });

        return originalIdStringPair;
    }

    public List<FactMessage> generateDeleteHearingPartFactMsg(List<HearingPart> hearingParts, FactsMapper factsMapper) {
        List<FactMessage> msgs = new ArrayList<>();

        hearingParts.forEach(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            msgs.add(new FactMessage(RulesService.DELETE_HEARING_PART, msg));
        });

        return msgs;
    }

    public List<FactMessage> generateUpsertHearingPartFactMsg(List<HearingPart> hearingParts, FactsMapper factsMapper) {
        List<FactMessage> msgs = new ArrayList<>();

        hearingParts.forEach(hp -> {
            String msg = factsMapper.mapHearingToRuleJsonMessage(hp);
            msgs.add(new FactMessage(RulesService.UPSERT_HEARING_PART, msg));
        });

        return msgs;
    }
}
