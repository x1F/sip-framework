package de.ikor.sip.foundation.core.declarative;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.connector.InboundConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioProviderDefinition;
import java.util.List;
import java.util.Optional;

/** API interface for {@link DeclarationsRegistry} used within the framework structure. */
public sealed interface DeclarationsRegistryApi permits DeclarationsRegistry {

  /**
   * Get {@link IntegrationScenarioDefinition} by its id.
   *
   * @param scenarioId is its id
   * @return Integration scenario
   */
  IntegrationScenarioDefinition getScenarioById(final String scenarioId);

  /**
   * Get {@link ConnectorDefinition} by its id.
   *
   * @param connectorId is its id
   * @return Optional connector
   */
  Optional<ConnectorDefinition> getConnectorById(final String connectorId);

  /**
   * Get list of all {@link InboundConnectorDefinition}.
   *
   * @return all inbound connectors
   */
  @SuppressWarnings("rawtypes")
  List<InboundConnectorDefinition> getInboundConnectors();

  /**
   * Get list of all {@link OutboundConnectorDefinition}.
   *
   * @return all outbound connectors
   */
  List<OutboundConnectorDefinition> getOutboundConnectors();

  /**
   * Get list of {@link InboundConnectorDefinition} by id of {@link IntegrationScenarioDefinition}.
   *
   * @param scenarioId is its id
   * @return inbound connectors
   */
  @SuppressWarnings("rawtypes")
  List<InboundConnectorDefinition> getInboundConnectorsByScenarioId(String scenarioId);

  /**
   * Get list of {@link OutboundConnectorDefinition} by id of {@link IntegrationScenarioDefinition}.
   *
   * @param scenarioId is its id
   * @return outbound connectors
   */
  List<OutboundConnectorDefinition> getOutboundConnectorsByScenarioId(String scenarioId);

  /**
   * Returns consumers of the composite process as defined in the annotation {@link
   * de.ikor.sip.foundation.core.declarative.annonation.CompositeProcess}
   *
   * @param compositeProcessID id of the process
   * @return List of {@link IntegrationScenarioDefinition}
   */
  List<IntegrationScenarioDefinition> getCompositeProcessConsumerDefinitions(
      String compositeProcessID);

  /**
   * Returns provider of the composite process as defined in the annotation {@link
   * de.ikor.sip.foundation.core.declarative.annonation.CompositeProcess}
   *
   * @param compositeProcessID id of the process
   * @return {@link IntegrationScenarioDefinition}
   */
  IntegrationScenarioDefinition getCompositeProcessProviderDefinition(String compositeProcessID);

  IntegrationScenarioBase getIntegrationScenarioBase(String compositeProcessID);

  /**
   * Returns all the processes that are providers for a scenario.
   *
   * @param integrationScenario that the processes provide to
   * @return List of {@link CompositeProcessDefinition}
   */
  List<CompositeProcessDefinition> getCompositeProcessProvidersForScenario(
      IntegrationScenarioDefinition integrationScenario);

  /**
   * Returns all the processes that are consumers from a scenario.
   *
   * @param integrationScenario that the processes consumes from
   * @return List of {@link CompositeProcessDefinition}
   */
  List<CompositeProcessDefinition> getCompositeProcessConsumersForScenario(
      IntegrationScenarioDefinition integrationScenario);

  /**
   * Returns all the providers for a scenario, they can be either {@link CompositeProcessDefinition}
   * or {@link InboundConnectorDefinition}
   *
   * @param integrationScenario that are being provided to
   * @return List of {@link IntegrationScenarioProviderDefinition}
   */
  List<IntegrationScenarioProviderDefinition> getProvidersForScenario(
      IntegrationScenarioDefinition integrationScenario);

  /**
   * Returns all the consumers for a scenario, they can be either {@link CompositeProcessDefinition}
   * or {@link OutboundConnectorDefinition}
   *
   * @param integrationScenario that are being consumed from
   * @return List of {@link IntegrationScenarioProviderDefinition}
   */
  List<IntegrationScenarioConsumerDefinition> getConsumersForScenario(
      IntegrationScenarioDefinition integrationScenario);
}
