package one.x1f.sip.foundation.core.apps.declarative;

import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.declarative.annotation.CompositeProcess;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.process.ProcessOrchestrator;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessBase;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @Filter(SIPIntegrationAdapter.class))
public class ProcessOrchestrationLoopAdapter {

  public static final String CONDITION_VALUE = "condition-name";
  private static final String ITERATIONS = "iterations";
  private final String GROUP_ID = "loop_group";

  public record CallLoopRequest(String name) {}

  public record CallLoopResponse(String name) {}

  public record FinalResponse(String name, String condition) {}

  @IntegrationScenario(
      scenarioId = CallLoopScenario.ID,
      requestModel = CallLoopRequest.class,
      responseModel = FinalResponse.class)
  public class CallLoopScenario extends IntegrationScenarioBase {

    public static final String ID = "CallLoopScenario";
  }

  @IntegrationScenario(
      scenarioId = InsideLoopScenario.ID,
      requestModel = CallLoopRequest.class,
      responseModel = CallLoopResponse.class)
  public class InsideLoopScenario extends IntegrationScenarioBase {

    public static final String ID = "InsideLoopScenario";
  }

  @IntegrationScenario(
      scenarioId = LoggingScenario.ID,
      requestModel = Object.class,
      responseModel = Object.class)
  public class LoggingScenario extends IntegrationScenarioBase {

    public static final String ID = "logging-scenario";
  }

  @IntegrationScenario(
      scenarioId = AfterLoopScenario.ID,
      requestModel = CallLoopResponse.class,
      responseModel = FinalResponse.class)
  public class AfterLoopScenario extends IntegrationScenarioBase {

    public static final String ID = "AfterLoopScenario";
  }

  @CompositeProcess(
      processId = LoopUntilConditionIsMetOrchestrator.ID,
      provider = CallLoopScenario.class,
      consumers = {AfterLoopScenario.class, InsideLoopScenario.class, LoggingScenario.class})
  public class LoopUntilConditionIsMetOrchestrator extends CompositeProcessBase {

    private static final String ID = "LoopUntilConditionIsMetOrchestrator";

    @Override
    public Orchestrator<CompositeProcessOrchestrationInfo> getOrchestrator() {
      return ProcessOrchestrator.forOrchestrationDsl(
          dsl -> {
            dsl.doWhile(
                    context ->
                        !"aaa".equals(context.getHeader(CONDITION_VALUE, String.class).get()))
                .callConsumer(InsideLoopScenario.class)
                .withRequestPreparation(
                    context -> {
                      var response = context.getOriginalRequest();
                      return response;
                    })
                .withNoResponseHandling()
                .endDoWhile()
                .forLoop(context -> context.getHeader(ITERATIONS, Integer.class).orElse(0))
                .callConsumer(LoggingScenario.class)
                .withRequestPreparation(
                    context -> {
                      var response = context.getLatestResponse().get();
                      return response;
                    })
                .withNoResponseHandling()
                .endForLoop()
                .callConsumer(AfterLoopScenario.class)
                .withRequestPreparation(
                    context -> {
                      var response = context.getLatestResponse();
                      return response.get();
                    });
          });
    }
  }

  @InboundConnector(
      connectorId = CallLoopInboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CallLoopScenario.ID,
      requestModel = String.class,
      responseModel = CallLoopResponse.class)
  public class CallLoopInboundConnector extends GenericInboundConnectorBase {

    public static final String ID = "CallLoopInboundConnector";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("CallLoopInboundConnector");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        String partnerName = e.getMessage().getBody(String.class);
                        e.getMessage().setHeader(CONDITION_VALUE, "");
                        e.getMessage().setHeader(ITERATIONS, 2);
                        e.getMessage().setBody(new CallLoopRequest(partnerName));
                      }));
    }
  }

  @OutboundConnector(
      connectorId = AfterLoopOutboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = AfterLoopScenario.ID,
      requestModel = CallLoopResponse.class,
      responseModel = FinalResponse.class)
  public class AfterLoopOutboundConnector extends GenericOutboundConnectorBase {

    public static final String ID = "AfterLoopOutboundConnector";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("AfterLoopOutboundConnector").plain(true);
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        CallLoopResponse response = e.getMessage().getBody(CallLoopResponse.class);
                        String finalValue = e.getMessage().getHeader(CONDITION_VALUE, String.class);
                        e.getMessage().setBody(new FinalResponse(response.name(), finalValue));
                      }));
    }
  }

  @OutboundConnector(
      connectorId = InsideLoopOutboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = InsideLoopScenario.ID,
      requestModel = CallLoopRequest.class,
      responseModel = CallLoopResponse.class)
  public class InsideLoopOutboundConnector extends GenericOutboundConnectorBase {

    public static final String ID = "InsideLoopOutboundConnector";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("InsideLoopOutboundConnector").plain(true);
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        String input = e.getMessage().getHeader(CONDITION_VALUE, String.class);
                        e.getMessage().setHeader(CONDITION_VALUE, input + "a");
                        e.getMessage()
                            .setBody(
                                new CallLoopResponse(
                                    e.getMessage().getBody(CallLoopRequest.class).name() + "Code"));
                      }));
    }
  }

  @OutboundConnector(
      connectorId = "out-logging-connector",
      connectorGroup = GROUP_ID,
      integrationScenario = LoggingScenario.ID,
      requestModel = CallLoopResponse.class,
      responseModel = CallLoopResponse.class)
  public class LoggingOutboundConnector extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("LoggingOutboundConnector").plain(true);
    }
  }
}
