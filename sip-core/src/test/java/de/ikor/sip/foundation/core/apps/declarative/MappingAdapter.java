package de.ikor.sip.foundation.core.apps.declarative;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.BackendTypes;
import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.BackendTypes.BackendResourceRequest;
import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.CommonDomainTypes.ResourceRequest;
import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.CommonDomainTypes.ResourceResponse;
import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.FrontEndSystemResponseMapper;
import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.FrontEndTypes.FrontEndSystemRequestMapper;
import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.FrontEndTypes.UserRequest;
import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.FrontEndTypes.UserResponse;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.annonation.UseRequestModelMapper;
import de.ikor.sip.foundation.core.declarative.annonation.UseResponseModelMapper;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.model.MarshallerDefinition;
import de.ikor.sip.foundation.core.declarative.model.UnmarshallerDefinition;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import java.util.Optional;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @Filter(SIPIntegrationAdapter.class))
public class MappingAdapter {

  @IntegrationScenario(
      scenarioId = MapDomainModelsScenario.ID,
      requestModel = ResourceRequest.class,
      responseModel = ResourceResponse.class)
  public class MapDomainModelsScenario extends IntegrationScenarioBase {
    public static final String ID = "MapDomainModels";
  }

  @InboundConnector(
      connectorGroup = "FrontEnd",
      integrationScenario = MapDomainModelsScenario.ID,
      requestModel = UserRequest.class,
      responseModel = UserResponse.class)
  @UseRequestModelMapper(FrontEndSystemRequestMapper.class)
  @UseResponseModelMapper(FrontEndSystemResponseMapper.class)
  public class RestInboundConnectorTestBase extends RestInboundConnectorBase {

    @Override
    protected void configureRest(RestDefinition definition) {
      definition
          .bindingMode(RestBindingMode.auto)
          .post("/user")
          .consumes("application/json")
          .type(UserRequest.class)
          .outType(UserResponse.class);
    }
  }

  @OutboundConnector(
      connectorGroup = "Backend",
      integrationScenario = MapDomainModelsScenario.ID,
      requestModel = BackendResourceRequest.class)
  @UseRequestModelMapper(BackendTypes.BackendRequestModelMapper.class)
  public class LoggerConsumerWithReponse extends GenericOutboundConnectorBase {

    @Override
    @Deprecated
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      ConnectorOrchestrator orchestrator =
          (ConnectorOrchestrator) super.defineTransformationOrchestrator();
      orchestrator.setResponseRouteTransformer(this::defineResponseRoute);
      return orchestrator;
    }

    @Override
    protected Optional<MarshallerDefinition> defineRequestMarshalling() {
      return Optional.of(MarshallerDefinition.forClause(DataFormatClause::json));
    }

    @Override
    protected Optional<UnmarshallerDefinition> defineResponseUnmarshalling() {
      return Optional.of(
          UnmarshallerDefinition.forClause(
              unmarshaller -> unmarshaller.json(BackendResourceRequest.class)));
    }

    protected void defineResponseRoute(final RouteDefinition definition) {
      // manually returning the test response
      definition.setBody(
          exchange ->
              ResourceResponse.builder()
                  .resourceType(
                      exchange.getIn().getBody(BackendResourceRequest.class).getResourceTypeName())
                  .resourceName("TEST")
                  .id(exchange.getIn().getBody(BackendResourceRequest.class).getId())
                  .build());
    }

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }
  }
}
