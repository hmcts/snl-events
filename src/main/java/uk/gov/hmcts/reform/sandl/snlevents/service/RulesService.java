package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sandl.snlevents.config.FactPropagationConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;

import java.util.List;
import java.util.UUID;

@Service
public class RulesService {
    public static final String INSERT_SESSION = "insert-session";
    public static final String UPSERT_SESSION = "upsert-session";
    public static final String UPSERT_HEARING_PART = "upsert-hearingPart";
    public static final String UPSERT_ROOM = "upsert-room";
    public static final String UPSERT_JUDGE = "upsert-judge";
    public static final String DELETE_SESSION = "delete-session";
    public static final String DELETE_HEARING_PART = "delete-hearingPart";
    public static final String UPSERT_SESSION_TYPE = "upsert-sessionType";

    private static final Logger logger = LoggerFactory.getLogger(RulesService.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${communication.searchUrl:http://localhost:8091/search}")
    private String searchUrl;

    @Autowired
    private FactMessageService factMessageService;

    @Autowired
    private FactPropagationConfiguration factPropagationConfiguration;

    @Autowired
    private S2SRulesAuthenticationClient s2sAuthService;

    public void postMessage(String msgType, String msgData) {
        postToSubscribers(null, new FactMessage(msgType, msgData));
    }

    public void postMessage(UUID userTransactionId,
                            String msgType, String msgData) {
        postToSubscribers(userTransactionId, new FactMessage(msgType, msgData));
    }

    public void postMessage(UUID userTransactionId, FactMessage factMessage) {
        postToSubscribers(userTransactionId, factMessage);
    }

    private void postToSubscribers(UUID userTransactionId, FactMessage msg) {
        HttpHeaders headers = this.s2sAuthService.createRulesAuthenticationHeader();
        HttpEntity<FactMessage> entity = new HttpEntity<>(msg, headers);

        List<String> subscribersEndpoints = factPropagationConfiguration.getMsgUrlsForMsgType(msg.getType());
        for (String endpoint : subscribersEndpoints) {
            logger.debug("Sending message type {} to {}", msg.getType(), endpoint);
            ResponseEntity<String> factMsg = postEntity(entity, endpoint);
            factMessageService.handle(userTransactionId, factMsg.getBody());
        }
    }

    @HystrixCommand
    private ResponseEntity<String> postEntity(HttpEntity<FactMessage> entity, String endpoint) {
        return restTemplate.postForEntity(endpoint, entity, String.class);
    }

    public List<FactMessage> getReloadStatuses() {
        // multiple threads?
        HttpHeaders headers = this.s2sAuthService.createRulesAuthenticationHeader();
        HttpEntity<FactMessage> entity = new HttpEntity<>(headers);
        // return restTemplate.exchange(searchUrl + params, HttpMethod.GET, entity, String.class).getBody();
        // TODO get from all rules engines
        // TODO unpack into reloadStatusFact
        return null;
    }
}
