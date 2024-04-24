package ${package}.connectorgroups.${connectorGroup2}.connectors;

import ${package}.scenarios.definitions.GetCityGeocodingScenario;
import ${package}.scenarios.models.GeoCodingRequest;
import ${package}.scenarios.models.GeoCodingResponse;
import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.model.UnmarshallerDefinition;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import java.util.Optional;
import org.apache.camel.Exchange;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.RouteDefinition;

/**
 * Outbound connector which calls
 * GET https://geocoding-api.open-meteo.com/v1/search?name={city}&count=1
 * The endpoint is used only for demo purposes
 */
@OutboundConnector(
        connectorGroup = "Group2",
        integrationScenario = GetCityGeocodingScenario.ID,
        requestModel = GeoCodingRequest.class,
        responseModel = GeoCodingResponse.class,
        connectorId = "GeoCodingOutboundConnector")
public class GeoCodingOutboundConnector extends GenericOutboundConnectorBase {

    // define external outbound endpoint
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
        return StaticEndpointBuilders.http("https", "geocoding-api.open-meteo.com/v1/search")
                .httpMethod("GET")
                .bridgeEndpoint(true);
    }

    // define unmarshalling method of response
    @Override
    protected Optional<UnmarshallerDefinition> defineResponseUnmarshalling() {
        return Optional.of(
                UnmarshallerDefinition.forDataFormat(new JacksonDataFormat(GeoCodingResponse.class)));
    }

    // define request/response transformation
    @Override
    public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
        return ConnectorOrchestrator.forConnector(this)
                .setRequestRouteTransformer(this::defineRequestRouteTransformer)
                .setResponseRouteTransformer(routeDefinition -> {
                    routeDefinition.process(exchange -> {
                        exchange.getMessage();
                    });
                });
    }

    private void defineRequestRouteTransformer(RouteDefinition routeDefinition) {
        routeDefinition.process(
                exchange -> {
                    GeoCodingRequest request = exchange.getMessage().getBody(GeoCodingRequest.class);
                    exchange.getMessage().removeHeader(Exchange.HTTP_PATH);
                    exchange.getMessage().setHeader(Exchange.HTTP_QUERY, "name=" + request.getCityName() + "&count=1");
                    exchange.getMessage().setBody("");
                });
    }
}
