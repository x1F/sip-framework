package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import one.x1f.sip.foundation.core.apps.declarative.ProcessOrchestrationAdapter.getPartnerDebtById;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.junit.jupiter.api.Test;

class CallProcessConsumerTest {

  @Test
  void WHEN_alreadyHasRequestPreparation_THEN_SIPExceptionOccurs() {

    // arrange
    final String MOCK_PROCESS_ID = "processMockId";
    CompositeProcessDefinition compositeProcess = mock(CompositeProcessDefinition.class);
    when(compositeProcess.getId()).thenReturn(MOCK_PROCESS_ID);

    CallProcessConsumer callProcessConsumer =
        new CallProcessConsumer(null, compositeProcess, getPartnerDebtById.class);
    callProcessConsumer.withRequestPreparation((context) -> null);

    // act & assert
    assertThatThrownBy(
            () -> {
              callProcessConsumer.withRequestPreparation((context) -> null);
            })
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessage(
            "Request preparation is already defined for the consumer '%s' for process '%s'. Chaining request preparation is not allowed.",
            getPartnerDebtById.ID, MOCK_PROCESS_ID);
  }
}
