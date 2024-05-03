package de.ikor.sip.foundation.core.declarative.orchestration.scenario.dsl;

import de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;

/** Interface containing various methods to trigger a call to an integration-scenario consumer. * */
sealed interface ScenarioConsumerCalls<S extends ScenarioConsumerCalls<S, R, M>, R, M>
    permits ConditionalCallScenarioConsumerDefinition.Branch,
        ForLoopCallScenarioConsumerDefinition.Branch,
        ForScenarioProvidersBaseDefinition,
        ScenarioConsumerCallsDelegate,
        WhileLoopCallScenarioConsumerDefinition.Branch {

  /**
   * Specifies that the outbound connector with the given <code>connectorId</code> should be called.
   *
   * @param connectorId Id of the outbound connector
   * @return DSL handle for further call instructions
   */
  CallScenarioConsumerByConnectorIdDefinition<S, M> callOutboundConnector(String connectorId);

  /**
   * Specifies that the outbound connector with the given <code>connectorClass</code> should be
   * called.
   *
   * @param connectorClass Class of the outbound connector
   * @return DSL handle for further call instructions
   */
  CallScenarioConsumerByClassDefinition<S, M> callOutboundConnector(
      Class<? extends OutboundConnectorDefinition> connectorClass);

  /**
   * Specifies that the scenario consumer with the given <code>consumerClass</code> should be
   * called.
   *
   * @param consumerClass Class of the consumer
   * @return DSL handle for further call instructions
   */
  CallScenarioConsumerByClassDefinition<S, M> callScenarioConsumer(
      Class<? extends IntegrationScenarioConsumerDefinition> consumerClass);
}
