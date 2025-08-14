package one.x1f.sip.foundation.core.apps.declarative;

import java.util.Map;
import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.connector.CleanupHeaders;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.RequestProcessor;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.apache.camel.Exchange;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestParamType;
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
  @CleanupHeaders(keep = "^(?!firstHiddenKey).*")
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

    @RequestProcessor
    public void process(Exchange exchange) {
      exchange.getMessage().setBody("");
    }
  }

  @OutboundConnector(
      connectorGroup = "test",
      integrationScenario = CleanupScenario.ID,
      requestModel = String.class,
      responseModel = String.class)
  @CleanupHeaders(keep = {"^(sec).+$", ".*hiddenKey.*"})
  public class HeaderManipulatingOutboundConnector extends GenericOutboundConnectorBase {
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.stub("http://somehost.here/header-test");
    }
  }
}
