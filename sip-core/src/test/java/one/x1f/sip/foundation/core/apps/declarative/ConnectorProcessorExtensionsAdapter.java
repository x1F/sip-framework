package one.x1f.sip.foundation.core.apps.declarative;

import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.apps.declarative.connectorextensions.RestStringAttachmentMapper;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.UseRequestModelMapper;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.*;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorProcessor;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.ComponentScan;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @ComponentScan.Filter(SIPIntegrationAdapter.class))
public class ConnectorProcessorExtensionsAdapter {

  public static final String INBOUND_DIRECT_OK = "inbound-direct-ok";

  @IntegrationScenario(
      scenarioId = ConnectorExtensionsScenario.ID,
      requestModel = String.class,
      responseModel = String.class)
  public class ConnectorExtensionsScenario extends IntegrationScenarioBase {
    public static final String ID = "ConnectorExtensionsScenario";
  }

  @InboundConnector(
      connectorGroup = "test",
      requestModel = String.class,
      responseModel = String.class,
      integrationScenario = ConnectorExtensionsScenario.ID)
  @UseRequestModelMapper(RestStringAttachmentMapper.class)
  public class RestParamMappingDirectInboundConnector extends GenericInboundConnectorBase {
    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct(INBOUND_DIRECT_OK);
    }

    @RequestProcessor
    @ExecuteBefore(RestStringAttachmentMapper.class)
    public void attachFirstString(Message message, String body) {
      message.setBody(message.getBody(String.class) + " first");
    }

    @RequestProcessor
    @ExecuteAfter(extensionName = "attachFirstString")
    public String attachSecondString(String body) {
      return body + " second";
    }

    @RequestExtension
    @ExecuteBefore(extensionName = "method-processor")
    public void attachReqExtension(RouteDefinition routeDef) {
      routeDef.process(
          exchange -> exchange.getMessage().setBody(exchange.getMessage().getBody() + " dslExt"));
    }

    @RequestProcessor
    @ExecutionOrder(1)
    public ConnectorProcessor attachProcessor() {
      return new ConnectorProcessor() {
        @Override
        public String getExtensionName() {
          return "method-processor";
        }

        @Override
        public void process(Exchange exchange) throws Exception {
          exchange.getMessage().setBody(exchange.getMessage().getBody(String.class) + " method");
        }
      };
    }
  }

  @OutboundConnector(
      connectorGroup = "test",
      requestModel = String.class,
      responseModel = String.class,
      integrationScenario = ConnectorExtensionsScenario.ID)
  public class RestParamMappingOutboundConnector extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }

    @ResponseProcessor
    public String attachFirstString(String body) {
      return body + " first";
    }

    @ResponseProcessor
    @ExecuteBefore(extensionName = "attachThirdString")
    public String attachSecondString(String body) {
      return body + " second";
    }

    @ResponseProcessor
    @ExecuteAfter(extensionName = "attachSecondString")
    public String attachThirdString(String body) {
      return body + " third";
    }

    @ResponseProcessor
    @ExecuteAfter(extensionName = "attachThirdString")
    public String attachFourthString(String body) {
      return body + " fourth";
    }

    @ResponseProcessor
    @ExecutionOrder(last = true)
    public String attachLastString(String body) {
      return body + " end";
    }
  }
}
