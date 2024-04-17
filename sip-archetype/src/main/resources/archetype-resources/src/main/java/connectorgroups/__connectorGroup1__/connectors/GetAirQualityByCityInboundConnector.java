package ${package}.connectorgroups.${connectorGroup1}.connectors;

import ${package}.scenarios.definitions.GetAirQualityByCityScenario;
import ${package}.scenarios.models.AirQualityResponse;
import ${package}.scenarios.models.GeoCodingRequest;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;

/**
 * Inbound connector which exposes GET /adapter/aircity/{city} endpoint.
 * Marks the entrypoint into the adapter.
 */
@InboundConnector(
        connectorId = "GetAirQualityByCityInboundConnector",
        connectorGroup = "Group1",
        requestModel = String.class,
        responseModel = AirQualityResponse.class,
        integrationScenario = GetAirQualityByCityScenario.ID)
public class GetAirQualityByCityInboundConnector extends RestInboundConnectorBase {

    public static final String CITY = "city";

    // define REST endpoint
    @Override
    protected void configureRest(RestDefinition restDefinition) {

        restDefinition
                .get("/aircity/{city}")
                .bindingMode(RestBindingMode.json)
                .tag("Air quality")
                .param()
                .name(CITY)
                .dataType("string")
                .type(RestParamType.path)
                .endParam()
                .outType(AirQualityResponse.class);
    }

    // define request/response transformation
    @Override
    public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
        return ConnectorOrchestrator.forConnector(this)
                .setRequestRouteTransformer(this::defineRequestRouteTransformer);
    }

    private void defineRequestRouteTransformer(RouteDefinition routeDefinition) {
        routeDefinition.process(
                exchange -> {
                    String cityName = exchange.getIn().getHeader(CITY, String.class);
                    exchange.getIn().setBody(GeoCodingRequest.builder().cityName(cityName).build());
                });
    }
}
