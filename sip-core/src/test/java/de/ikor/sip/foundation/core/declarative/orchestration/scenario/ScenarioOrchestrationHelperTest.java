package de.ikor.sip.foundation.core.declarative.orchestration.scenario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.Test;

class ScenarioOrchestrationHelperTest {

  @Test
  void When_setExchangeInContext_Expect_ExchangeSet() {
    // arrange
    Exchange exchange = mock(Exchange.class);
    ScenarioOrchestrationContext scenarioOrchestrationContext =
        ScenarioOrchestrationContext.builder().build();

    // act
    ScenarioOrchestrationHelper.setExchangeInContext(scenarioOrchestrationContext, exchange);

    // assert
    assertThat(scenarioOrchestrationContext.getExchange()).isEqualTo(exchange);
  }
}
