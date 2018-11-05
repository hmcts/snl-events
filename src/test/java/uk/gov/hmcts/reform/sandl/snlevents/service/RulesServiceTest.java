package uk.gov.hmcts.reform.sandl.snlevents.service;

import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sandl.snlevents.config.SubscribersConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RulesServiceTest {

    public static final String BODY = "body";
    public static final String TYPE = "1";
    public static final String DATA = "data";
    public static final String ENDPOINT = "endpoint";

    @InjectMocks
    RulesService rulesService;

    @Mock
    private FactMessageService factMessageService;

    @Mock
    private SubscribersConfiguration subscribersConfiguration;

    @Mock
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Mock
    private RestTemplate restTemplate = mock(RestTemplate.class);

    @Test
    public void postMessage_postsToFactMessageService_whenMatchingSubscriberIsFound() throws IOException {
        //GIVEN there's a subscriber for type "1"
        when(subscribersConfiguration.getSubscribers()).thenReturn(createSubscribers(TYPE));
        when(restTemplate.postForEntity(anyString(), anyString(), any()))
            .thenReturn(createResponseEntity());

        //WHEN we post fact of type "1"
        rulesService.postMessage(TYPE, DATA);

        //THEN fact is posted
        verify(factMessageService, times(1)).handle(any(UUID.class), eq(BODY));
    }

    @Test
    public void postMessage_doesNothing_whenNoMatchingSubscriberIsFound() throws IOException {
        //GIVEN there's a subscriber for type "another"
        when(subscribersConfiguration.getSubscribers()).thenReturn(createSubscribers("another"));
        when(restTemplate.postForEntity(anyString(), anyString(), any()))
            .thenReturn(createResponseEntity());

        //WHEN we post fact of type "1"
        rulesService.postMessage(TYPE, DATA);

        //THEN fact is not posted
        verify(factMessageService, times(0)).handle(any(UUID.class), anyString());
    }

    private ResponseEntity createResponseEntity() {
        return new ResponseEntity<>(BODY, HttpStatus.OK);
    }

    private Map<String,List<String>> createSubscribers(String type) {
        val subscriber = Arrays.asList(ENDPOINT);

        val subscribers = new HashMap<String, List<String>>();
        subscribers.put(type, subscriber);

        return subscribers;
    }

    @Test
    public void search_returnsEntity() {
        val response = createResponseEntity();

        when(restTemplate.exchange(anyString(), any(), any(), isA(Class.class)))
            .thenReturn(response);

        val returnedResponse = rulesService.search("");

        assertThat(returnedResponse).isEqualTo(BODY);
    }
}
