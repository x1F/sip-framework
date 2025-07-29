package de.ikor.sip.foundation.core.apps.declarative;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.declarative.annonation.CompositeProcess;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.annonation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.RequestProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ResponseProcessor;
import de.ikor.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.process.ProcessOrchestrator;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessBase;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @Filter(SIPIntegrationAdapter.class))
public class ProcessOrchestrationSplitAdapter {

  private final String GROUP_ID = "split_group";

  public record CallSplitRequest(List<String> names) {}

  public record CallSplitResponse(List<String> updatedNames) {}

  @IntegrationScenario(
      scenarioId = CallSplitScenario.ID,
      requestModel = Object.class,
      responseModel = CallSplitResponse.class)
  public class CallSplitScenario extends IntegrationScenarioBase {

    public static final String ID = "CallSplitScenario";
  }

  @IntegrationScenario(
      scenarioId = InsideSplitScenario.ID,
      requestModel = String.class,
      responseModel = String.class)
  public class InsideSplitScenario extends IntegrationScenarioBase {

    public static final String ID = "InsideSplitScenario";
  }

  @IntegrationScenario(
      scenarioId = AfterSplitScenario.ID,
      requestModel = CallSplitResponse.class,
      responseModel = CallSplitResponse.class)
  public class AfterSplitScenario extends IntegrationScenarioBase {

    public static final String ID = "AfterSplitScenario";
  }

  @CompositeProcess(
      processId = SplitBodyOrchestrator.ID,
      provider = CallSplitScenario.class,
      consumers = {AfterSplitScenario.class, InsideSplitScenario.class})
  public class SplitBodyOrchestrator extends CompositeProcessBase {

    private static final String ID = "SplitBodyOrchestrator";

    @Override
    public Orchestrator<CompositeProcessOrchestrationInfo> getOrchestrator() {
      return ProcessOrchestrator.forOrchestrationDsl(
          dsl -> {
            dsl.split(context -> context.getOriginalRequest(CallSplitRequest.class).names())
                .callConsumer(InsideSplitScenario.class)
                .withResponseHandling(
                    (latestResponse, context) -> {
                      var res = context.getProcessResponse();
                      if (res.isPresent()) {
                        ((CallSplitResponse) res.get())
                            .updatedNames()
                            .add(latestResponse.toString());
                      } else {
                        List<String> processRes = new ArrayList<>();
                        processRes.add(latestResponse.toString());
                        context.setProcessResponse(
                            new CallSplitResponse(processRes), Optional.empty());
                      }
                    })
                .endSplit()
                .callConsumer(AfterSplitScenario.class)
                .withRequestPreparation(
                    context -> {
                      var response = context.getProcessResponse();
                      return response.get();
                    });
          });
    }
  }

  @InboundConnector(
      connectorId = CallSplitInboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CallSplitScenario.ID,
      requestModel = Object.class,
      responseModel = CallSplitResponse.class)
  public class CallSplitInboundConnector extends GenericInboundConnectorBase {

    public static final String ID = "CallSplitInboundConnector";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("CallSplitInboundConnector");
    }

    @RequestProcessor
    public CallSplitRequest handleRequest() {
      return new CallSplitRequest(List.of("John", "Jane"));
    }
  }

  @OutboundConnector(
      connectorId = AfterSplitOutboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = AfterSplitScenario.ID,
      requestModel = CallSplitResponse.class,
      responseModel = CallSplitResponse.class)
  public class AfterSplitOutboundConnector extends GenericOutboundConnectorBase {

    public static final String ID = "AfterSplitOutboundConnector";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("AfterSplitOutboundConnector").plain(true);
    }

    @ResponseProcessor
    public void handleResponse(CallSplitResponse response) {
      response.updatedNames().add("Jon Doe");
    }
  }

  @OutboundConnector(
      connectorId = InsideSplitOutboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = InsideSplitScenario.ID,
      requestModel = String.class,
      responseModel = String.class)
  public class InsideSplitOutboundConnector extends GenericOutboundConnectorBase {

    public static final String ID = "InsideSplitOutboundConnector";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("InsideSplitOutboundConnector").plain(true);
    }

    @ResponseProcessor
    public String handleResponse(String response) {
      return response + " Doe";
    }
  }
}
