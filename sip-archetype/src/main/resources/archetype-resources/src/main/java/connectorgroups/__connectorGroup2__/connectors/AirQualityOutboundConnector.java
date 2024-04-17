package ${package}.connectorgroups.${connectorGroup2}.connectors;

import ${package}.scenarios.definitions.GetAirQualityLatLonScenario;
import ${package}.scenarios.models.AirQualityRequest;
import ${package}.scenarios.models.AirQualityResponse;
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
 * GET https://air-quality-api.open-meteo.com/v1/air-quality?latitude={lat}&longitude={lon}&hourly=pm10,pm2_5
 * The endpoint is used only for demo purposes
 */
@OutboundConnector(
        connectorGroup = "Group2",
        integrationScenario = GetAirQualityLatLonScenario.ID,
        requestModel = String.class,
        responseModel = AirQualityResponse.class,
        connectorId = "AirQualityOutboundConnector")
public class AirQualityOutboundConnector extends GenericOutboundConnectorBase {

    // define external outbound endpoint
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
        return StaticEndpointBuilders.http("https", "air-quality-api.open-meteo.com/v1/air-quality")
                .httpMethod("GET")
                .bridgeEndpoint(true);
    }

    // define unmarshalling method of response
    @Override
    protected Optional<UnmarshallerDefinition> defineResponseUnmarshalling() {
        return Optional.of(
                UnmarshallerDefinition.forDataFormat(new JacksonDataFormat(AirQualityResponse.class)));
    }

    // define request/response transformation
    @Override
    public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
        return ConnectorOrchestrator.forConnector(this).setRequestRouteTransformer(this::setRequest)
                .setResponseRouteTransformer(routeDefinition -> {
                    routeDefinition.process(exchange -> {
                        exchange.getMessage();
                    });
                });
    }

    private void setRequest(RouteDefinition routeDefinition) {
        routeDefinition.process(
                exchange -> {
                    AirQualityRequest request = exchange.getMessage().getBody(AirQualityRequest.class);
                    exchange.getMessage().removeHeader(Exchange.HTTP_PATH);
                    exchange.getMessage()
                            .setHeader(
                                    Exchange.HTTP_QUERY,
                                    "latitude="
                                            + request.getLat()
                                            + "&longitude="
                                            + request.getLon()
                                            + "&hourly=pm10,pm2_5");
                });
    }
}
