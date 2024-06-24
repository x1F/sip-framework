package ${package}.connectorgroups.${connectorGroup1}.connectors;

import ${package}.config.NobelPrizeMapper;
import ${package}.connectorgroups.${connectorGroup1}.models.NobelPrizeCategory;
import ${package}.scenarios.definitions.GetNobelPrizeAndLaureateDetails;
import ${package}.scenarios.models.NobelPrizeCommonModel;
import ${package}.scenarios.models.NobelPrizeRequest;
import ${package}.scenarios.models.response.NobelPrizeResponse;
import de.ikor.sip.foundation.core.declarative.annonation.ConnectorExceptionHandler;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import de.ikor.sip.foundation.core.declarative.annotation.rest.PathParameter;
import de.ikor.sip.foundation.core.declarative.configuration.ConnectorOnExceptionDefinition;
import de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
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
        responseModel = NobelPrizeCommonModel.class,
        integrationScenario = GetNobelPrizeAndLaureateDetails.ID)
@RequiredArgsConstructor
public class GetNobelPrizeDetailsInConnector extends RestInboundConnectorBase {

    private final NobelPrizeMapper nobelPrizeMapper;

    // Define REST endpoint
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

    // Extract path parameter, map them into common model and set it as request body
    @ParameterMapping
    public void mapQueryParameters(
            Message message,
            @PathParameter("category") NobelPrizeCategory category,
            @PathParameter("year") String year) {
        final var mappedData =
                NobelPrizeRequest.builder()
                        .category(category.getValue())
                        .year(year)
                        .build();
        message.setBody(mappedData);
    }

    // Define request/response transformation.
    // Default empty transformers exist,
    // overriding this method is used to replace them when custom transformation is required.
    @Override
    public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
        return ConnectorOrchestrator.forConnector(this)
                .setResponseRouteTransformer(this::setResponse);
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

    @ConnectorExceptionHandler(IllegalArgumentException.class)
    public ConnectorOnExceptionDefinition handleIllegalArgumentException(){
        return onExceptionDefinition -> onExceptionDefinition
                .process(exchange -> {
                    String message = exchange
                            .getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class)
                            .getMessage();
                    exchange.getMessage().setBody(message);
                    exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 400);
                })
                .handled(true);
    }
}