package de.ikor.sip.foundation.core.declarative;

import lombok.AllArgsConstructor;
import lombok.Getter;

/** Enum containing the different roles of a route. */
@AllArgsConstructor
public enum RouteRole {
  EXTERNAL_ENDPOINT("externalEndpoint", "EXTERNAL_ENDPOINT"),
  EXTERNAL_SOAP_SERVICE_PROXY("externalSoapServiceProxy", "EXTERNAL_SOAP_SERVICE_PROXY"),
  CONNECTOR_REQUEST_ORCHESTRATION("requestOrchestration", "CONNECTOR_REQUEST_ORCHESTRATION"),
  CONNECTOR_RESPONSE_ORCHESTRATION("responseOrchestration", "CONNECTOR_RESPONSE_ORCHESTRATION"),
  SCENARIO_HANDOFF("scenarioHandoff", "SCENARIO_HANDOFF"),
  SCENARIO_TAKEOVER("scenarioTakeover", "SCENARIO_TAKEOVER"),
  SCENARIO_ORCHESTRATION("scenarioOrchestration", "SCENARIO_ORCHESTRATION"),
  COMPOSITE_SCENARIO_ORCHESTRATION(
      "compositeScenarioOrchestration", "COMPOSITE_SCENARIO_ORCHESTRATION");

  @Getter final String roleSuffixInRouteId;
  @Getter final String externalName;
}
