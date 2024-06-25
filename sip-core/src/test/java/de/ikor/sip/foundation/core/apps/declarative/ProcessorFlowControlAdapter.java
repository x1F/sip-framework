package de.ikor.sip.foundation.core.apps.declarative;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.annonation.UseRequestModelMapper;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteBefore;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.RequestProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import de.ikor.sip.foundation.core.declarative.annotation.rest.PathParameter;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.model.ModelMapper;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import lombok.Builder;
import lombok.Data;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.model.rest.RestDefinition;
import org.springframework.context.annotation.ComponentScan;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @ComponentScan.Filter(SIPIntegrationAdapter.class))
public class ProcessorFlowControlAdapter {

  @Data
  @Builder
  public static class Input {
    final String originalInput;
    String currentInput;
  }

  @Data
  @Builder
  public static class Output {
    final String originalOutput;
    String currentOutput;
  }

  @IntegrationScenario(
      scenarioId = ProcessorFlowControlScenario.ID,
      requestModel = Input.class,
      responseModel = Output.class)
  public class ProcessorFlowControlScenario extends IntegrationScenarioBase {
    public static final String ID = "ProcessorFlowControlScenario";
  }

  @InboundConnector(
      connectorGroup = "test",
      requestModel = String.class,
      responseModel = String.class,
      integrationScenario = ProcessorFlowControlScenario.ID)
  @UseRequestModelMapper(ProcessorFlowInboundConnector.RequestMapper.class)
  public class ProcessorFlowInboundConnector extends RestInboundConnectorBase {

    public static class RequestMapper implements ModelMapper<String, Input> {
      @Override
      public Input mapToTargetModel(final String bodyContent) {
        return Input.builder().originalInput(bodyContent).currentInput(bodyContent).build();
      }
    }

    @Override
    protected void configureRest(final RestDefinition definition) {
      definition
          .bindingMode(RestBindingMode.auto)
          .get("/run/{additional}/{stuff}")
          .consumes("application/json")
          .type(String.class)
          .outType(String.class);
    }

    @ParameterMapping
    public void mapQueryParameters(
        Input mappedInput,
        @PathParameter("additional") String additional,
        @PathParameter("stuff") String stuff) {
      mappedInput.setCurrentInput(mappedInput.getCurrentInput() + additional + stuff);
    }

    @RequestProcessor
    @ExecuteBefore(RequestMapper.class)
    public void processInputToUpper(Exchange exc, Message msg, String input) {
      msg.setHeader("INPUT_WAS_UPPERCASED", true);
      msg.setBody(input.toUpperCase());
    }

    @RequestProcessor
    @ExecuteBefore(processorName = "processInputToUpper")
    public SeparateProcessorImplementation processInputMore() {
      return new SeparateProcessorImplementation();
    }

    @Override
    @Deprecated
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return super.defineTransformationOrchestrator();
    }
  }

  @RequestProcessor(ProcessorFlowInboundConnector.class)
  @ExecuteBefore(processorName = "processInputToUpper")
  public static class SeparateProcessorImplementation implements ConnectorProcessor {
    @Override
    public void process(final Exchange exchange) throws Exception {}
  }

  @OutboundConnector(
      connectorGroup = "test",
      requestModel = String.class,
      responseModel = String.class,
      integrationScenario = ProcessorFlowControlScenario.ID)
  public class ProcessorFlowOutboundConnector extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }
  }
}
