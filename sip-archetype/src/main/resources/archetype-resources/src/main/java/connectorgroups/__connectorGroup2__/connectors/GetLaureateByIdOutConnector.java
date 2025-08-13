package ${package}.connectorgroups.${connectorGroup2}.connectors;

import ${package}.scenarios.definitions.GetLaureateById;
import ${package}.scenarios.models.laureate.Laureate;
import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.model.UnmarshallerDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.RouteDefinition;

import java.util.Optional;

/**
 * Outbound connector which calls the public Nobel Prize API:
 * GET https://api.nobelprize.org/2.0/laureate/{id}
 * It fetches the data of a Laureate by their id.
 */
@OutboundConnector(
    connectorGroup = "be",
    integrationScenario = GetLaureateById.ID,
    requestModel = Integer.class,
    responseModel = Laureate[].class,
    connectorId = "GetLaureateByIdOutConnector")
public class GetLaureateByIdOutConnector extends GenericOutboundConnectorBase {

  // Define external outbound endpoint
  @Override
  protected EndpointProducerBuilder defineOutgoingEndpoint() {
    return StaticEndpointBuilders.http("https", "api.nobelprize.org/2.0/laureate/${body}")
        .bridgeEndpoint(true);
  }

  // Define request/response transformation
  @Override
  public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
    return ConnectorOrchestrator.forConnector(this).setRequestRouteTransformer(this::setRequest);
  }

  private void setRequest(RouteDefinition routeDefinition) {
    routeDefinition.process(
        exchange -> {
          // Required by Nobel Prize API, otherwise the response is compressed and cannot be processed
          exchange.getMessage().setHeader("Accept-Encoding", "deflate");
        });
  }

  // Define unmarshalling method of response
  @Override
  protected Optional<UnmarshallerDefinition> defineResponseUnmarshalling() {
    return Optional.of(
        UnmarshallerDefinition.forDataFormat(new JacksonDataFormat(Laureate[].class)));
  }
}
