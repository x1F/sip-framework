package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import lombok.AccessLevel;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/** DSL class for calling an outbound connector specified by its ID */
public final class CallScenarioConsumerByConnectorIdDefinition<R, M>
    extends CallScenarioConsumerBaseDefinition<
        CallScenarioConsumerByConnectorIdDefinition<R, M>, R, M> {

  @Getter(AccessLevel.PACKAGE)
  private final String connectorId;

  CallScenarioConsumerByConnectorIdDefinition(
      final R dslReturnDefinition,
      final IntegrationScenarioDefinition integrationScenario,
      final String connectorId) {
    super(dslReturnDefinition, integrationScenario);
    this.connectorId = connectorId;
  }
}
