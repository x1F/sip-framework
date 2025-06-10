package de.ikor.sip.foundation.core.declarative.connector;

import org.apache.camel.AggregationStrategy;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.model.RouteDefinition;

public abstract class PollingOutboundConnectorBase extends GenericOutboundConnectorBase {

  @Override
  public void defineOutboundEndpoints(final RouteDefinition routeDefinition) {
    defineRequestMarshalling().ifPresent(marshaller -> marshaller.accept(routeDefinition));
    EndpointConsumerBuilder endpoint = definePollingEndpoint();
    routeDefinition.pollEnrich(endpoint, defineAggregationStrategy())
            .id(routeDefinition.getRouteId());
    defineResponseUnmarshalling().ifPresent(unmarshaller -> unmarshaller.accept(routeDefinition));
  }

  protected AggregationStrategy defineAggregationStrategy() {
    return (oldExchange, newExchange) -> newExchange;
  }

  protected abstract EndpointConsumerBuilder definePollingEndpoint();

  protected EndpointProducerBuilder defineOutgoingEndpoint() { return null; }
}
