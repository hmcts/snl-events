package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sandl.snlevents.config.SubscribersConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RulesService {
    public static final String INSERT_SESSION = "insert-session";
    public static final String UPSERT_SESSION = "upsert-session";
    public static final String UPSERT_HEARING_PART = "upsert-hearingPart";
    public static final String UPSERT_ROOM = "upsert-room";
    public static final String UPSERT_JUDGE = "upsert-judge";
    public static final String UPSERT_AVAILABILITY = "upsert-availability";
    public static final String DELETE_SESSION = "delete-session";

    private static final Logger logger = LoggerFactory.getLogger(RulesService.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${communication.searchUrl:http://localhost:8091/search}")
    private String searchUrl;

    @Autowired
    private FactMessageService factMessageService;

    @Autowired
    private SubscribersConfiguration subscribersConfiguration;

    public void postMessage(String msgType, String msgData) throws IOException {
        postToSubscribers(null, new FactMessage(msgType, msgData));
    }

    public void postMessage(UUID userTransactionId,
                            String msgType, String msgData) throws IOException {
        postToSubscribers(userTransactionId, new FactMessage(msgType, msgData));
    }

    private void postToSubscribers(UUID userTransactionId, FactMessage msg) throws IOException {
        Map<String, List<String>> subscribers = subscribersConfiguration.getSubscribers();

        if (subscribers.containsKey(msg.getType())) {
            List<String> subscribersEndpoints = subscribers.get(msg.getType());
            for (String endpoint : subscribersEndpoints) {
                logger.debug("Sending message type {} to {}", msg.getType(), endpoint);
                ResponseEntity<String> factMsg = restTemplate.postForEntity(endpoint, msg, String.class);
                factMessageService.handle(userTransactionId, factMsg.getBody());
            }
        }
    }

    public String search(String params) {
        return restTemplate.getForEntity(searchUrl + params, String.class).getBody();
    }
}
