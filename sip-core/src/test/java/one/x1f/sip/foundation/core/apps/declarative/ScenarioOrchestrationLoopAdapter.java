package one.x1f.sip.foundation.core.apps.declarative;

import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @Filter(SIPIntegrationAdapter.class))
public class ScenarioOrchestrationLoopAdapter {

  public static final String CONDITION_VALUE = "condition-name";
  private final String GROUP_ID = "loop_group";

  public record CallLoopRequest(String name) {}

  public record CallLoopResponse(String name) {}

  public record FinalResponse(String name, String condition) {}

  @IntegrationScenario(
      scenarioId = CallLoopScenario.ID,
      requestModel = Object.class,
      responseModel = Object.class)
  public class CallLoopScenario extends IntegrationScenarioBase {

    public static final String ID = "CallLoopScenario";

    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
      return ScenarioOrchestrator.forOrchestrationDslWithResponse(
          Object.class,
          dsl ->
              dsl.forInboundConnectors(CallLoopInboundConnector.class)
                  .doWhile(
                      context ->
                          !"aaa".equals(context.getHeader(CONDITION_VALUE, String.class).get()))
                  .callOutboundConnector(InsideLoopOutboundConnector.class)
                  .andNoResponseHandling()
                  .endDoWhile()
                  .forLoop(context -> 2)
                  .callOutboundConnector(InsideLoopOutboundConnector.class)
                  .withRequestPreparation(ScenarioOrchestrationContext::getOriginalRequest)
                  .andNoResponseHandling()
                  .endForLoop()
                  .callOutboundConnector(AfterLoopOutboundConnector.class)
                  .withRequestPreparation(
                      context -> {
                        var response = context.getResponse();
                        return response.get();
                      })
                  .andNoResponseHandling());
    }
  }

  @InboundConnector(
      connectorId = CallLoopInboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CallLoopScenario.ID,
      requestModel = String.class,
      responseModel = Object.class)
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
                        e.getMessage().setBody(new CallLoopRequest(partnerName));
                      }));
    }
  }

  @OutboundConnector(
      connectorId = AfterLoopOutboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CallLoopScenario.ID,
      requestModel = Object.class,
      responseModel = Object.class)
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
      integrationScenario = CallLoopScenario.ID,
      requestModel = Object.class,
      responseModel = Object.class)
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
      integrationScenario = CallLoopScenario.ID,
      requestModel = Object.class,
      responseModel = Object.class)
  public class LoggingOutboundConnector extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("Logging").plain(true);
    }
  }
}
