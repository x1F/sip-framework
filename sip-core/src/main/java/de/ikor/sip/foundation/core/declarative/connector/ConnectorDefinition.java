package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.DeclarativeElement;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestratable;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Common definition interface for specifying connectors used within a SIP adapter.
 *
 * <p>A connector is responsible for defining the endpoints for communication with external systems,
 * as well as transforming the request- and response-models between the external system's domain and
 * the integration scenario's common domain model.
 *
 * <p><em>Adapter developers should not implement this interface directly, but use one of the more
 * specific {@link InboundConnectorDefinition} or {@link OutboundConnectorDefinition} interfaces
 * instead.</em>
 *
 * @see InboundConnectorDefinition
 * @see OutboundConnectorDefinition
 */
public sealed interface ConnectorDefinition
    extends Orchestratable<ConnectorOrchestrationInfo>, DeclarativeElement
    permits ConnectorBase,
        ConnectorDefinition.None,
        InboundConnectorDefinition,
        OutboundConnectorDefinition {

  /**
   * Returns the type of the connector.
   *
   * @return Connector type
   */
  ConnectorType getConnectorType();

  /**
   * Returns the ID of the connector group this connector belongs to.
   *
   * @see de.ikor.sip.foundation.core.declarative.connectorgroup.ConnectorGroupDefinition
   * @return ID of the connector group
   */
  String getConnectorGroupId();

  /**
   * Returns the ID of the integration scenario this connector belongs to.
   *
   * @see de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition
   * @return ID of the integration scenario
   */
  String getScenarioId();

  /**
   * Returns the base class of the request model used by the connector. This is the request model
   * that is used by the adapter to communicate with the external system.
   *
   * @return Request model base class
   */
  Class<?> getRequestModelClass();

  /**
   * Returns the base class of the response model used by the connector. This is the response model
   * that is used by the adapter to communicate with the external system.
   *
   * @return Response model base class, or an empty {@link Optional} if the connector does not have
   *     a response flow
   */
  Optional<Class<?>> getResponseModelClass();

  /**
   * Empty {@link ConnectorDefinition} implementation that is used in default values for annotations
   */
  final class None implements ConnectorDefinition {

    @Override
    public ConnectorType getConnectorType() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getConnectorGroupId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getScenarioId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getRequestModelClass() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Class<?>> getResponseModelClass() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String[] getConfigurationIds() {
      return new String[0];
    }

    @Override
    public List<Method> getOnExceptionHandler() {
      return Collections.emptyList();
    }

    @Override
    public String getId() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String getPathToDocumentationResource() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
      throw new UnsupportedOperationException();
    }
  }

  /**
   * Returns an array of configuration ids which should be applied to the given connector routes
   *
   * @return configuration ids
   */
  String[] getConfigurationIds();

  /**
   * Returns a list of {@link Method}s which contain exception handling logic for the connector
   *
   * @return list of {@link Method}
   */
  List<Method> getOnExceptionHandler();
}
