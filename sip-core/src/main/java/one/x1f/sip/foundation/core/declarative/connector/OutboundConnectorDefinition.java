package one.x1f.sip.foundation.core.declarative.connector;

import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import org.apache.camel.model.RouteDefinition;

/**
 * Definition of an <em>outbound</em> connector, which is a specialization of a {@link
 * ConnectorDefinition} that forwards an integration call to an external system and, if necessary,
 * receives its response.
 *
 * <p>The outbound connector is responsible for defining the endpoint(s) that forwards the
 * integration call to the external system. The endpoint(s) are defined by the {@link
 * #defineOutboundEndpoints(RouteDefinition)} method.
 *
 * <p>Adapter developers should not implement this interface directly, but rather extend one of the
 * outbound {@link ConnectorBase} subclasses and annotate it with @{@link OutboundConnector}.
 *
 * @see ConnectorDefinition
 * @see GenericOutboundConnectorBase
 * @see OutboundConnector
 */
public non-sealed interface OutboundConnectorDefinition
    extends ConnectorDefinition, IntegrationScenarioConsumerDefinition {

  /**
   * Defines the outbound endpoint(s) for the integration scenario the connector belongs to.
   *
   * @param routeDefinition Route definition that the outbound endpoint(s) should be added to.
   */
  void defineOutboundEndpoints(RouteDefinition routeDefinition);

  @Override
  default ConnectorType getConnectorType() {
    return ConnectorType.OUT;
  }

  String getScenarioId();
}
