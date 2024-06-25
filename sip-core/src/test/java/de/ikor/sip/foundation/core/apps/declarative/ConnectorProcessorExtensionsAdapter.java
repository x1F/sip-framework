package de.ikor.sip.foundation.core.apps.declarative;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.apps.declarative.connectorextensions.RestStringAttachmentMapper;
import de.ikor.sip.foundation.core.declarative.annonation.*;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteOrder;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.RequestProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteAfter;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteBefore;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;
import de.ikor.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.springframework.context.annotation.ComponentScan;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @ComponentScan.Filter(SIPIntegrationAdapter.class))
public class ConnectorProcessorExtensionsAdapter {

  public static final String INBOUND_DIRECT_OK = "inbound-direct-ok";
  public static final String METHOD_PROCESSOR_ATTACHEMENT_STRING = "method-processor";

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
    @ExecuteAfter(processorName = "attachFirstString")
    public String attachSecondString(String body) {
      return body + " second";
    }

    @RequestProcessor
    @ExecuteOrder(1)
    public ConnectorProcessor attachProcessor() {
      return new ConnectorProcessor() {
        @Override
        public String getProcessorName() {
          return "method-processor";
        }

        @Override
        public void process(Exchange exchange) throws Exception {
          exchange.getMessage().setBody(exchange.getMessage().getBody(String.class) + " " + METHOD_PROCESSOR_ATTACHEMENT_STRING);
        }
      };
    }
  }

  @OutboundConnector(
      connectorGroup = "test",
      requestModel = String.class,
      integrationScenario = ConnectorExtensionsScenario.ID)
  public class RestParamMappingOutboundConnector extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }
  }
}
