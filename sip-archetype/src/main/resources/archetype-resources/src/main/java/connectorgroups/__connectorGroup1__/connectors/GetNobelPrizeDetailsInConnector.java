package ${package}.connectorgroups.${connectorGroup1}.connectors;

import ${package}.config.NobelPrizeMapper;
import ${package}.connectorgroups.${connectorGroup1}.models.NobelPrizeCategory;
import ${package}.scenarios.definitions.GetNobelPrizeAndLaureateDetails;
import ${package}.scenarios.models.NobelPrizeCommonModel;
import ${package}.scenarios.models.NobelPrizeRequest;
import ${package}.scenarios.models.response.NobelPrizeResponse;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import lombok.RequiredArgsConstructor;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;

import java.util.StringJoiner;

/**
 * Inbound connector which exposes GET /adapter/nobelprize/{category}/{year} endpoint.
 * Used to fetch details about a Nobel Prize and its Laureates for a certain category and year.
 */
@InboundConnector(
        connectorId = "GetNobelPrizeDetailsInConnector",
        connectorGroup = "fe",
        requestModel = String.class,
        responseModel = Object.class,
        integrationScenario = GetNobelPrizeAndLaureateDetails.ID)
@RequiredArgsConstructor
public class GetNobelPrizeDetailsInConnector extends RestInboundConnectorBase {

    private final NobelPrizeMapper nobelPrizeMapper;

    // define REST endpoint
    @Override
    protected void configureRest(RestDefinition restDefinition) {
        restDefinition.get("/nobelprize/{category}/{year}")
                .tag("Nobel Prize")
                .outType(NobelPrizeResponse.class)
                .param()
                .name("category")
                .allowableValues(getNobelPrizeCategoryValues())
                .type(RestParamType.path)
                .endParam()
                .param()
                .name("year")
                .type(RestParamType.path)
                .endParam();
    }

    // define request/response transformation
    // Default empty transformers exist,
    // overriding this method is used to replace them when custom transformation is required
    @Override
    public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
        return ConnectorOrchestrator.forConnector(this)
                .setRequestRouteTransformer(this::setRequest)
                .setResponseRouteTransformer(this::setResponse);
    }

    private void setRequest(RouteDefinition routeDefinition) {
        routeDefinition.process(
                exchange -> {
                    NobelPrizeRequest nobelPrizeRequest =
                            NobelPrizeRequest.builder()
                                    .category(exchange.getMessage()
                                            .getHeader("category", NobelPrizeCategory.class)
                                            .getValue())
                                    .year(exchange.getMessage().getHeader("year", String.class))
                                    .build();
                    exchange.getMessage().setBody(nobelPrizeRequest);
                });
    }

    private void setResponse(RouteDefinition routeDefinition) {
        routeDefinition.process(
                exchange -> {
                    NobelPrizeCommonModel nobelPrizeCommonModel = exchange.getMessage().getBody(NobelPrizeCommonModel.class);
                    NobelPrizeResponse nobelPrizeResponse =
                            nobelPrizeMapper.toNobelPrizeResponse(nobelPrizeCommonModel.getNobelPrize());
                    nobelPrizeResponse.setLaureates(nobelPrizeMapper.toLaureates(nobelPrizeCommonModel.getLaureates()));
                    exchange.getMessage().setBody(nobelPrizeResponse);
                });
    }

    private String getNobelPrizeCategoryValues() {
        StringJoiner joiner = new StringJoiner(",");
        for (NobelPrizeCategory category : NobelPrizeCategory.values()) {
            joiner.add(category.toString().toLowerCase());
        }
        return joiner.toString();
    }
}