package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.DeclarationsRegistry;
import de.ikor.sip.foundation.core.declarative.RoutesRegistry;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioProviderDefinition;
import java.util.List;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.model.OptionalIdentifiedDefinition;

/**
 * Definition of an <em>inbound</em> connector, which is a specialization of a {@link
 * ConnectorDefinition} that initiates an integration call for the integration scenario the
 * connector belongs to.
 *
 * <p>The inbound connector is responsible for defining the endpoint that initiates the integration
 * call. The endpoint is defined by the {@link #defineInboundEndpoints(OptionalIdentifiedDefinition,
 * String, RoutesRegistry)} method.
 *
 * <p>Adapter developers should not implement this interface directly, but rather extend one of the
 * inbound {@link ConnectorBase} subclasses and annotate it with @{@link
 * de.ikor.sip.foundation.core.declarative.annonation.InboundConnector}.
 *
 * @param <T> Type that is required by the inbound connector to define the endpoint. This is usually
 *     a {@link org.apache.camel.model.RoutesDefinition}.
 * @see ConnectorDefinition
 * @see GenericInboundConnectorBase
 * @see RestInboundConnectorBase
 * @see de.ikor.sip.foundation.core.declarative.annonation.InboundConnector
 */
public non-sealed interface InboundConnectorDefinition<T extends OptionalIdentifiedDefinition<T>>
    extends ConnectorDefinition, IntegrationScenarioProviderDefinition {

  /**
   * Defines the inbound endpoint(s) for the integration scenario the connector belongs to.
   *
   * @param definition Type that is required to initiate the endpoint.
   * @param targetToBase Target that the inbound endpoint should be connected to (e.g. the final
   *     {@link org.apache.camel.model.ProcessorDefinition#to(EndpointProducerBuilder)} in a {@link
   *     org.apache.camel.model.RouteDefinition}).
   * @param routeRegistry Route registry that must be used to register routeIds for the inbound
   *     endpoint(s).
   */
  List<String> defineInboundEndpoints(
          T definition, String targetToBase, RoutesRegistry routeRegistry, DeclarationsRegistry declarationsRegistry);

  @Override
  default ConnectorType getConnectorType() {
    return ConnectorType.IN;
  }

  String getScenarioId();

  /**
   * Returns the class of the type that is required to define the inbound endpoint(s).
   *
   * @return Class of the type that is required to define the inbound endpoint(s).
   */
  Class<? extends T> getEndpointDefinitionTypeClass();
}
