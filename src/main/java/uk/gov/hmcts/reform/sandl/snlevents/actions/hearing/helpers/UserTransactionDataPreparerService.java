package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

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

    public void prepareUserTransactionDataForUpdate(String entity, UUID entityId, String previousEntityString,
                                                           int counterActionOrder) {

        String action = "update";
        String counterAction = "update";
        userTransactionDataList.add(new UserTransactionData(entity,
            entityId,
            previousEntityString,
            action,
            counterAction,
            counterActionOrder)
        );
    }

    public void prepareLockedEntityTransactionData(String entity, UUID entityId, int counterActionOrder) {
        String action = "lock";
        String counterAction = "unlock";
        userTransactionDataList.add(new UserTransactionData(entity, entityId, null,
            action, counterAction, counterActionOrder));
    }

    public Map<UUID, String> mapHearingPartsToStrings(ObjectMapper objectMapper, List<HearingPart> hearingParts) {
        Map<UUID, String> originalIdStringPair = new HashMap<>();
        hearingParts.stream().forEach(hp -> {
            try {
                String hearingPartString = objectMapper.writeValueAsString(hp);
                originalIdStringPair.put(hp.getId(), hearingPartString);
            } catch (JsonProcessingException e) {
                throw new SnlRuntimeException(e);
            }
        });

        return originalIdStringPair;
    }
}
