package de.ikor.sip.foundation.core.declarative.orchestration.scenario.dsl;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import de.ikor.sip.foundation.core.declarative.annotation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.junit.jupiter.api.Test;

class ScenarioConsumerCallsDelegateTest {

  @IntegrationScenario(scenarioId = "test", requestModel = String.class)
  class TestScenario extends IntegrationScenarioBase {}

  @Test
  void
      When_NoResponseInScenario_With_ResponseHandlerInOrchestration_Then_ThrowInitializationException() {
    var s =
        new CallScenarioConsumerBaseNoResponseDefinition(
            mock(), new TestScenario(), GenericOutboundConnectorBase.class);

    assertThatThrownBy(() -> s.andHandleResponse((a, b) -> {}))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessageContaining(
            "Integration Scenario test does not have a response model defined, using response handler is not intended.");
  }

  @Test
  void
      When_NoResponseInScenario_With_AggregationInOrchestration_Then_ThrowInitializationException() {
    var s =
        new CallScenarioConsumerBaseNoResponseDefinition(
            mock(), new TestScenario(), GenericOutboundConnectorBase.class);

    assertThatThrownBy(() -> s.andAggregateResponse((a, b) -> a))
        .isInstanceOf(SIPFrameworkInitializationException.class)
        .hasMessageContaining(
            "Integration Scenario test does not have a response model defined, using aggregate response is not intended.");
  }
}
