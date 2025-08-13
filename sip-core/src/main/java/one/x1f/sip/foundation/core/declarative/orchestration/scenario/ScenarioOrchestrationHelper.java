package one.x1f.sip.foundation.core.declarative.orchestration.scenario;

import lombok.experimental.UtilityClass;
import org.apache.camel.Exchange;

/** Helper class for Scenario Orchestration */
@UtilityClass
public class ScenarioOrchestrationHelper {
  /**
   * Set exchange in ScenarioOrchestrationContext
   *
   * @param scenarioOrchestrationContext {@link ScenarioOrchestrationContext} where Exchange should
   *     be set
   * @param exchange {@link Exchange}
   */
  public static void setExchangeInContext(
      ScenarioOrchestrationContext<?> scenarioOrchestrationContext, Exchange exchange) {
    scenarioOrchestrationContext.setExchange(exchange);
  }
}
