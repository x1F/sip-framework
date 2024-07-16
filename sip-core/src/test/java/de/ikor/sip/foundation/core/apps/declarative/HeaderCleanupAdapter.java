package de.ikor.sip.foundation.core.apps.declarative;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.connector.CleanupHeaders;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import java.util.Map;
import java.util.function.Function;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @ComponentScan.Filter(SIPIntegrationAdapter.class))
public class HeaderCleanupAdapter {

  @IntegrationScenario(
      scenarioId = CleanupScenario.ID,
      requestModel = String.class,
      responseModel = String.class)
  public class CleanupScenario extends IntegrationScenarioBase {
    public static final String ID = "cleanupScenario";
  }

  @InboundConnector(
      connectorGroup = "test",
      integrationScenario = CleanupScenario.ID,
      requestModel = String.class,
      responseModel = Map.class)
  public class CleanupRestInboundConnector extends RestInboundConnectorBase {

    @Override
    protected void configureRest(final RestDefinition definition) {
      definition
          .get("/test")
          .bindingMode(RestBindingMode.auto)
          .type(String.class)
          .outType(Map.class)
          .param()
          .name("firstHeader")
          .type(RestParamType.header)
          .dataType("string")
          .endParam()
          .param()
          .name("secondHeader")
          .type(RestParamType.header)
          .dataType("string")
          .endParam()
          .param()
          .name("secondary")
          .type(RestParamType.header)
          .dataType("string")
          .endParam();
    }
  }

  @OutboundConnector(
      connectorGroup = "test",
      integrationScenario = CleanupScenario.ID,
      requestModel = String.class,
      responseModel = String.class)
  @CleanupHeaders(keep = {"sec.+", "firstHeader"})
  public class HeaderManipulatingOutboundConnector extends GenericOutboundConnectorBase {

    public static final String BEAN_NAME = "headerManipulatingBean";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.bean(BEAN_NAME);
    }

    @Bean(BEAN_NAME)
    public Function<Object, String> endpointBean() {
      return data -> "response";
    }
  }
}
