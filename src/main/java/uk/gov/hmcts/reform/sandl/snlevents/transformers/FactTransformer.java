package uk.gov.hmcts.reform.sandl.snlevents.transformers;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class FactTransformer {
    private static Map<String, String> nonDirectFactsToEntityMap;

    private FactTransformer(){} // for checkstyle compliance

    static {
        nonDirectFactsToEntityMap = new HashMap<>();
        nonDirectFactsToEntityMap.put("judge", "person");
    }

    public static String transformToEntityName(String fact) {
        String factForEntity = fact.toLowerCase(Locale.ENGLISH);
        String entityName = FactTransformer.nonDirectFactsToEntityMap.get(factForEntity);
        if (entityName == null) {
            //system will map only special cases
            entityName = factForEntity;
        }
        return entityName;
    }
}
