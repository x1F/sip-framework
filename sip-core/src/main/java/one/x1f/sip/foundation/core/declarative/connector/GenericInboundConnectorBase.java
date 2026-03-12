package one.x1f.sip.foundation.core.declarative.connector;

import static one.x1f.sip.foundation.core.declarative.AdapterBuilder.*;
import static one.x1f.sip.foundation.core.declarative.utils.DeclarativeHelper.*;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import one.x1f.sip.foundation.core.declarative.ConnectorRegistry;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.RouteRole;
import one.x1f.sip.foundation.core.declarative.RoutesRegistry;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.dto.ProcessorType;
import one.x1f.sip.foundation.core.declarative.model.MarshallerDefinition;
import one.x1f.sip.foundation.core.declarative.model.UnmarshallerDefinition;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.RoutesDefinition;

/**
 * Base class for defining generic inbound connectors.
 *
 * <p>Adapter developers should extend this class and annotate it with @{@link InboundConnector} to
 * specify the connector. The configuration of the inbound endpoint is done by overriding {@link
 * #defineInitiatingEndpoint()}.
 *
 * @see ConnectorBase#defineTransformationOrchestrator() Infos on attaching transformation between
 *     domain models of connector and integration scenario
 * @see InboundConnector
 */
public abstract class GenericInboundConnectorBase extends InboundConnectorBase
    implements InboundConnectorDefinition<RoutesDefinition> {

  @Override
  public final void defineInboundEndpoints(
      final RoutesDefinition definition,
      final String targetToBase,
      final RoutesRegistry routeRegistry,
      final DeclarationsRegistry declarationsRegistry,
      final ConnectorRegistry connectorRegistry) {
    String routeConfigurationIds =
        joinConfigurationIds(
            this.getId(),
            this.getConfigurationIds(),
            this.getScenario().get().getConfigurationIds());
    AtomicInteger order = new AtomicInteger();
    EndpointConsumerBuilder endpoint = resolveForbiddenEndpoint(defineInitiatingEndpoint());
    String routeId = routeRegistry.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, this);
    final var routeDef =
        definition.from(endpoint).routeId(routeId).routeConfigurationId(routeConfigurationIds);
    appendOnException(this, routeDef, declarationsRegistry);

    defineRequestUnmarshalling()
        .ifPresent(
            unmarshaller -> {
              connectorRegistry.registerProcessorExtension(
                  routeId,
                  getId() + UNMARSHALLING_SUFFIX,
                  order.get(),
                  UNMARSHALLING_LABEL,
                  endpoint.getRawUri(),
                  ProcessorType.UNMARSHALLER);
              unmarshaller.accept(routeDef);
              order.getAndIncrement();
            });
    routeDef.to(StaticEndpointBuilders.direct(targetToBase));
    connectorRegistry.registerProcessorExtension(
        routeId,
        getId() + "_inbound_entry",
        order.get(),
        endpoint.getRawUri(),
        endpoint.getRawUri(),
        ProcessorType.ENTRY);
    order.getAndIncrement();
    defineResponseMarshalling()
        .ifPresent(
            marshaller -> {
              connectorRegistry.registerProcessorExtension(
                  routeId,
                  getId() + MARSHALLING_SUFFIX,
                  order.get(),
                  MARSHALLING_LABEL,
                  endpoint.getRawUri(),
                  ProcessorType.MARSHALLER);
              marshaller.accept(routeDef);
            });
  }

  /**
   * Handle meant to be overloaded if the definition of an unmarshaller for the request type is
   * needed.
   *
   * @return Unmarshaller for the request type
   */
  protected Optional<UnmarshallerDefinition> defineRequestUnmarshalling() {
    return Optional.empty();
  }

  /**
   * Handle meant to be overloaded if the definition of a marshaller for the response type is
   * needed.
   *
   * @return Marshaller for response type
   */
  protected Optional<MarshallerDefinition> defineResponseMarshalling() {
    return Optional.empty();
  }

  /**
   * Defines the initiating endpoint for this connector.
   *
   * @see org.apache.camel.builder.endpoint.StaticEndpointBuilders
   * @see org.apache.camel.builder.endpoint.dsl.FileEndpointBuilderFactory.FileEndpointBuilder
   * @return the initiating endpoint
   */
  protected abstract EndpointConsumerBuilder defineInitiatingEndpoint();

  @Override
  public final Class<RoutesDefinition> getEndpointDefinitionTypeClass() {
    return RoutesDefinition.class;
  }
}
