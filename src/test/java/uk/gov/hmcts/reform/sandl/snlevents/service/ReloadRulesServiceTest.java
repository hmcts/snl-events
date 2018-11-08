package uk.gov.hmcts.reform.sandl.snlevents.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.sandl.snlevents.config.FactPropagationConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.config.FactPropagationEngineConfiguration;
import uk.gov.hmcts.reform.sandl.snlevents.mappers.FactsMapper;
import uk.gov.hmcts.reform.sandl.snlevents.model.db.Room;
import uk.gov.hmcts.reform.sandl.snlevents.model.rules.FactReloadStatus;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.HearingPartRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.PersonRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.RoomRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionRepository;
import uk.gov.hmcts.reform.sandl.snlevents.repository.db.SessionTypeRepository;
import uk.gov.hmcts.reform.sandl.snlevents.security.S2SRulesAuthenticationClient;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ReloadRulesServiceTest {

    public static final String BODY = "body";
    public static final String ENDPOINT = "endpoint";

    @InjectMocks
    ReloadRulesService reloadRulesService;

    @Mock
    private FactMessageService factMessageService;

    @Mock
    private RulesService rulesService;

    @Mock
    private FactPropagationConfiguration factPropagationConfiguration;

    @Mock
    private S2SRulesAuthenticationClient s2SRulesAuthenticationClient;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private PersonRepository personRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HearingPartRepository hearingPartRepository;

    @Mock
    private SessionTypeRepository sessionTypeRepository;

    @Mock
    private FactsMapper factsMapper;

    @Mock
    private Clock clock;

    @Mock
    private RestTemplate restTemplate = mock(RestTemplate.class);

    @Before
    public void before() {
        val engine = mock(FactPropagationEngineConfiguration.class);
        when(engine.getMsgUrl()).thenReturn(ENDPOINT);
        when(engine.getReloadStatusUrl()).thenReturn(ENDPOINT);

        when(factPropagationConfiguration.getEngines()).thenReturn(Arrays.asList(engine));
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        when(sessionTypeRepository.findAll()).thenReturn(new ArrayList<>());
        when(roomRepository.findAll()).thenReturn(Arrays.asList(new Room()));
        when(personRepository.findPeopleByPersonTypeEqualsIgnoreCase(any())).thenReturn(new ArrayList<>());
        when(sessionRepository.findAll()).thenReturn(new ArrayList<>());
        when(hearingPartRepository.findAll()).thenReturn(new ArrayList<>());
    }

    @Test
    public void getReloadStatuses_returnsStatuses() {
        val now = OffsetDateTime.now(UTC);
        when(restTemplate.exchange(eq(ENDPOINT), any(), any(), any(Class.class)))
            .thenReturn(createReloadStatusResponse(new FactReloadStatus(now, now.plusMinutes(1))));

        val res = reloadRulesService.getReloadStatuses();

        assertThat(res).hasSize(1);
        assertThat(res.get(0).getReloadStatus().getStartedAt()).isEqualByComparingTo(now);
    }

    @Test
    public void reloadDateAndTimeIfNeeded_changes_date_parts_only_when_needed() throws JsonProcessingException {
        OffsetDateTime now = OffsetDateTime.of(2018, 11, 11, 14, 10, 0, 0, ZoneOffset.UTC);
        when(clock.instant()).thenReturn(now.toInstant());

        doNothing().when(rulesService).postMessage(any(String.class), any(String.class));

        reloadRulesService.reloadDateAndTimeIfNeeded();
        // first time when started it should send all of them as they have changed in the cached values from
        // default int to actual values
        verify(rulesService, times(1)).postMessage(eq("upsert-year"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-month"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-day"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-hour"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-minute"), any());

        reloadRulesService.reloadDateAndTimeIfNeeded();
        // second time they should not change
        verify(rulesService, times(1)).postMessage(eq("upsert-year"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-month"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-day"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-hour"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-minute"), any());

        when(clock.instant()).thenReturn(now.plusMinutes(1).toInstant());
        reloadRulesService.reloadDateAndTimeIfNeeded();
        // now we only want minute to change other not
        verify(rulesService, times(1)).postMessage(eq("upsert-year"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-month"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-day"), any());
        verify(rulesService, times(1)).postMessage(eq("upsert-hour"), any());
        verify(rulesService, times(2)).postMessage(eq("upsert-minute"), any());
    }

    @Test
    public void reloadIfNeeded_reload_if_not_loaded() throws IOException {
        val now = OffsetDateTime.now(UTC);
        when(restTemplate.exchange(eq(ENDPOINT), any(), any(), any(Class.class)))
            .thenReturn(createReloadStatusResponse(null));
        when(restTemplate.postForEntity(any(), any(), any(), Mockito.<Object>anyVararg()))
            .thenReturn(new ResponseEntity<>(BODY, HttpStatus.OK));
        doNothing().when(factMessageService).handle(any(), any());

        reloadRulesService.reloadIfNeeded();

        verify(restTemplate, atLeastOnce()).postForEntity(any(), any(), any(), Mockito.<Object>anyVararg());
        verify(factMessageService, atLeastOnce()).handle(any(), any());
    }

    @Test
    public void reloadIfNeeded_do_not_reload_if_loaded_already() throws IOException {
        val now = OffsetDateTime.now(UTC);
        when(restTemplate.exchange(eq(ENDPOINT), any(), any(), any(Class.class)))
            .thenReturn(createReloadStatusResponse(new FactReloadStatus(now, now.plusMinutes(1))));

        when(restTemplate.postForEntity(any(), any(), any(), Mockito.<Object>anyVararg()))
            .thenReturn(new ResponseEntity<>(BODY, HttpStatus.OK));

        reloadRulesService.reloadIfNeeded();

        verify(restTemplate, times(0)).postForEntity(any(), any(), any());
    }

    private ResponseEntity createReloadStatusResponse(FactReloadStatus factReloadStatus) {
        return new ResponseEntity<>(factReloadStatus, HttpStatus.OK);
    }
}
