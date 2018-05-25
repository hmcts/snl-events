package uk.gov.hmcts.reform.sandl.snlevents.transformers;

import java.util.HashMap;
import java.util.Map;

public class FactTransformer {
    private FactTransformer(){} // for checkstyle compliance

    private static Map<String, String> nonDirectFactsToEntityMap;

    static {
        nonDirectFactsToEntityMap = new HashMap<>();
        nonDirectFactsToEntityMap.put("judge", "person");
    }

    public static String transformToEntityName(String fact) {
        fact = fact.toLowerCase();
        String entityName = FactTransformer.nonDirectFactsToEntityMap.get(fact);
        if (entityName == null) {
            //system will map only special cases
            entityName = fact;
        }
        return entityName;
    }
}
