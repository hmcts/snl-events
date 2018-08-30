package uk.gov.hmcts.reform.sandl.snlevents.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.sandl.snlevents.actions.testactions.RulesProcessableTestAction;
import uk.gov.hmcts.reform.sandl.snlevents.actions.testactions.TestAction;

import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ActionServiceTest {

    @InjectMocks
    private ActionService actionService;

    @Spy
    private TestAction mockTestAction;

    @Spy
    private RulesProcessableTestAction mockRulesProcessableTestAction;

    @Mock
    private UserTransactionService uts;

    @Mock
    private RulesService rulesService;

    @Test
    public void execute_CallsMethodsInProperOrder() {
        when(uts.isAnyBeingTransacted(any())).thenReturn(false);
        when(mockTestAction.getAssociatedEntitiesIds()).thenReturn(new UUID[0]);

        actionService.execute(mockTestAction);

        verify(uts, times(0)).transactionConflicted(any());
        verify(mockTestAction).getUserTransactionId();
        verify(mockTestAction).getAndValidateEntities();
        verify(mockTestAction).getAssociatedEntitiesIds();
        verify(mockTestAction).act();
        verify(mockTestAction).generateUserTransactionData();
        verify(mockTestAction).generateUserTransactionData();

        verify(uts, times(1)).isAnyBeingTransacted();
    }

    @Test
    public void execute_ReturnsConflictedWhenTransactionInProgress() {
        when(uts.isAnyBeingTransacted()).thenReturn(true);

        actionService.execute(mockTestAction);

        verify(mockTestAction, times(0)).act();
        verify(uts, times(1)).transactionConflicted(any());
        verify(uts, times(1)).isAnyBeingTransacted();
    }

    @Test
    public void execute_CallsRulesServiceWhenInstanceOfRulesProcessable() {
        UUID id = UUID.randomUUID();
        when(mockRulesProcessableTestAction.getUserTransactionId()).thenReturn(id);

        actionService.execute(mockRulesProcessableTestAction);

        verify(rulesService).postMessage(Mockito.eq(id), any());
    }
}
