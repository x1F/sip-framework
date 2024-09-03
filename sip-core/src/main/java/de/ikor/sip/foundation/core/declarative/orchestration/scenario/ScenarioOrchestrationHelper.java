package de.ikor.sip.foundation.core.declarative.orchestration.scenario;

import org.apache.camel.Exchange;

public class ScenarioOrchestrationHelper {
  public static void setExchange(
      ScenarioOrchestrationContext scenarioOrchestrationContext, Exchange exchange) {
    scenarioOrchestrationContext.setExchange(exchange);
  }
}
