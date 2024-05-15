package ${package}.connectorgroups.${connectorGroup2}.connectors;

import ${package}.scenarios.definitions.GetLaureateById;
import ${package}.scenarios.models.laureate.Laureate;
import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.model.UnmarshallerDefinition;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import org.apache.camel.Exchange;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.RouteDefinition;

import java.util.Optional;

import static org.apache.camel.Exchange.HTTP_PATH;

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
    return StaticEndpointBuilders.http("https", "api.nobelprize.org/2.0/laureate")
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
          exchange.getMessage().setHeader(HTTP_PATH, exchange.getMessage().getBody(Integer.class));
          // required by Nobel Prize API
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
