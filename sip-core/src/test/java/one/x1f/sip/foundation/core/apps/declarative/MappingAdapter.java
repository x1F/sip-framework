package one.x1f.sip.foundation.core.apps.declarative;

import java.util.Optional;
import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.BackendTypes;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.BackendTypes.BackendResourceRequest;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.CommonDomainTypes.ResourceRequest;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.CommonDomainTypes.ResourceResponse;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.FrontEndSystemResponseMapper;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.FrontEndTypes.FrontEndSystemRequestMapper;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.FrontEndTypes.UserRequest;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.FrontEndTypes.UserResponse;
import one.x1f.sip.foundation.core.declarative.annotation.*;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.RequestProcessor;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.ResponseProcessor;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.model.MarshallerDefinition;
import one.x1f.sip.foundation.core.declarative.model.UnmarshallerDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.apache.camel.Exchange;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.component.jackson3.JacksonDataFormat;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestDefinition;
import org.springframework.context.annotation.Bean;
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

    @RequestProcessor
    public void process(Exchange exchange) {
      exchange.getMessage();
    }

    @ResponseProcessor
    public void process2(Exchange exchange) {
      exchange.getMessage();
    }
  }

  @OutboundConnector(
      connectorGroup = "Backend",
      integrationScenario = MapDomainModelsScenario.ID,
      requestModel = BackendResourceRequest.class)
  @UseRequestModelMapper(BackendTypes.BackendRequestModelMapper.class)
  public class LoggerConsumerWithResponse extends GenericOutboundConnectorBase {

    @Override
    protected Optional<MarshallerDefinition> defineRequestMarshalling() {
      return Optional.of(MarshallerDefinition.forClause(DataFormatClause::json));
    }

    @Override
    protected Optional<UnmarshallerDefinition> defineResponseUnmarshalling() {
      return Optional.of(
          UnmarshallerDefinition.forDataFormat(
              new JacksonDataFormat(BackendResourceRequest.class)));
    }

    @ResponseProcessor
    public ResourceResponse defineResponseRoute(Exchange exchange) {
      // manually returning the test response
      return ResourceResponse.builder()
          .resourceType(
              exchange.getIn().getBody(BackendResourceRequest.class).getResourceTypeName())
          .resourceName("TEST")
          .id(exchange.getIn().getBody(BackendResourceRequest.class).getId())
          .build();
    }

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }
  }

  @Bean
  public FrontEndSystemRequestMapper frontEndSystemRequestMapperBean() {
    return new FrontEndSystemRequestMapper();
  }
}
