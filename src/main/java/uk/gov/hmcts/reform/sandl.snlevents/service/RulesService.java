package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sandl.snlevents.messages.FactMessage;

import java.io.IOException;

@Service
public class RulesService {
    public static final String INSERT_SESSION = "insert-session";

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private FactMessageService factMessageService;

    @Value("${communication.rulesUrl}")
    private String rulesUrl;

    public void postMessage(String msgType, String msgData) throws IOException {
        FactMessage msg = new FactMessage(msgType, msgData);

        ResponseEntity<String> factMsg = restTemplate.postForEntity(rulesUrl + "/msg", msg, String.class);
        factMessageService.handle(factMsg.getBody());
    }
}
