package uk.gov.hmcts.reform.sandl.snlevents.actions.hearing.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.hmcts.reform.sandl.snlevents.exceptions.SnlRuntimeException;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Hearing;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.HearingPart;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Session;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.UserTransactionData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class UserTransactionDataPreparerService {

    public static List<UserTransactionData> generateUserTransactionData(Hearing hearing, String previousHearing,
                                                                        List<HearingPart> hearingParts,
                                                                        ObjectMapper objectMapper,
                                                                        List<Session> sessions, String action,
                                                                        String counterAction) {

        Map<UUID, String> originalHearingParts = mapHearingPartsToStrings(hearingParts, objectMapper);

        List<UserTransactionData> userTransactionDataList = new ArrayList<>();
        originalHearingParts.forEach((id, hpString) ->
            userTransactionDataList.add(new UserTransactionData("hearingPart",
                id,
                hpString,
                action,
                counterAction,
                0)
            )
        );

        userTransactionDataList.add(new UserTransactionData("hearing",
            hearing.getId(),
            previousHearing,
            action,
            counterAction,
            1)
        );

        sessions.stream().forEach(s ->
            userTransactionDataList.add(prepareLockedEntityTransactionData("session", s.getId()))
        );

        return userTransactionDataList;
    }

    private static Map<UUID, String> mapHearingPartsToStrings(List<HearingPart> hearingParts,
                                                              ObjectMapper objectMapper) {

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

    private static UserTransactionData prepareLockedEntityTransactionData(String entity, UUID id) {
        return new UserTransactionData(entity, id, null, "lock", "unlock", 0);
    }

    private UserTransactionDataPreparerService() {
    }
}
