package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sandl.snlevents.config.FactPropagationConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class RulesServiceTest {

    public static final String BODY = "body";
    public static final String TYPE = "1";
    public static final String DATA = "data";

    @InjectMocks
    RulesService rulesService;

    @Mock
    private FactMessageService factMessageService;

    @Mock
    private FactPropagationConfiguration factPropagationConfiguration;

    @Mock
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Mock
    private RestTemplate restTemplate = mock(RestTemplate.class);

    @Test
    public void postMessage_postsToFactMessageService_whenMatchingSubscriberIsFound() throws IOException {
        //GIVEN there's a subscriber for type "1"
        when(factPropagationConfiguration.getMsgUrlsForMsgType(TYPE)).thenReturn(Arrays.asList(TYPE));
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
        when(factPropagationConfiguration.getMsgUrlsForMsgType(TYPE)).thenReturn(new ArrayList<>());
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
}
