package de.ikor.sip.foundation.core.apps.declarative;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import de.ikor.sip.foundation.core.declarative.annotation.rest.PathParameter;
import de.ikor.sip.foundation.core.declarative.annotation.rest.QueryParameter;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestDefinition;
import org.springframework.context.annotation.ComponentScan;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @ComponentScan.Filter(SIPIntegrationAdapter.class))
public class RestParamMappingAdapter {

  @Data
  @Jacksonized
  @Builder
  public static class RestMappedData {
    String queryParam;
    Integer pathParamFirst;
    String pathParamSecond;
    String body;
  }

  @IntegrationScenario(
      scenarioId = RestParamMappingScenario.ID,
      requestModel = RestMappedData.class,
      responseModel = RestMappedData.class)
  public class RestParamMappingScenario extends IntegrationScenarioBase {
    public static final String ID = "RestParamMappingScenario";
  }

  @InboundConnector(
      connectorGroup = "test",
      requestModel = String.class,
      responseModel = RestMappedData.class,
      integrationScenario = RestParamMappingScenario.ID)
  public class RestParamMappingInboundConnector extends RestInboundConnectorBase {

    @Override
    protected void configureRest(final RestDefinition definition) {
      definition
          .bindingMode(RestBindingMode.auto)
          .get("/mapper/{first}/{second}")
          .consumes("application/json")
          .type(String.class)
          .outType(RestMappedData.class);
    }

    @ParameterMapping
    public void mapQueryParameters(
        Exchange exchange,
        Message message,
        String body,
        @PathParameter("first") Integer pathFirst,
        @PathParameter("second") String pathSecond,
        @QueryParameter("query") String query) {
      final var mappedData =
          RestMappedData.builder()
              .body(body)
              .pathParamFirst(pathFirst)
              .pathParamSecond(pathSecond)
              .queryParam(query)
              .build();
      message.setBody(mappedData);
    }
  }

  @OutboundConnector(
      connectorGroup = "test",
      requestModel = RestMappedData.class,
      integrationScenario = RestParamMappingScenario.ID)
  public class RestParamMappingOutboundConnector extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }
  }
}
