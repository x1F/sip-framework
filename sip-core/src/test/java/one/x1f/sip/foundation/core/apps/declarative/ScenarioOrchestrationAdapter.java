package one.x1f.sip.foundation.core.apps.declarative;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.StepResultCloner;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
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
public class ScenarioOrchestrationAdapter {

  private final String GROUP_ID = "group";

  // <-- ORCHESTRATED SCENARIO START
  @Data
  @AllArgsConstructor
  public static class ScenarioResponse implements Serializable, Cloneable {

    public ScenarioResponse(ScenarioResponse response) {
      this.id = response.id;
      this.value = response.value;
    }

    @Override
    public ScenarioResponse clone() throws CloneNotSupportedException {
      ScenarioResponse response = (ScenarioResponse) super.clone();
      response.setId(id);
      response.setValue(value);
      return response;
    }

    private String id;

    private Integer value;
  }

  @IntegrationScenario(
      scenarioId = CustomOrchestrationScenario.ID,
      requestModel = String.class,
      responseModel = ScenarioResponse.class)
  public class CustomOrchestrationScenario extends IntegrationScenarioBase {

    public static final String ID = "TestScenario";

    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
      return ScenarioOrchestrator.forOrchestrationDslWithResponse(
          ScenarioResponse.class,
          dsl -> {
            // first connector only calls first outbound connector
            dsl.forInboundConnectors(InboundConnectorOne.class, InboundConnectorTwo.class)
                .callOutboundConnector(OutboundConnectorOne.ID)
                .withRequestPreparation(
                    context -> context.getOriginalRequest() + "-scenarioprepared")
                .andNoResponseHandling();

            // second connectors calls second, first and third outbound connectors and sums the
            // results. Sum also depends on the order of the connectors (first one is multiplied by
            // 10, second one by 100, third by 1000)
            dsl.forInboundConnectors(InboundConnectorThree.ID)
                .callOutboundConnector(OutboundConnectorTwo.class)
                .withResponseCloner(StepResultCloner.forCloneable())
                .withRequestPreparation(
                    context -> context.getOriginalRequest() + "-scenarioprepared")
                .andNoResponseHandling()
                .callOutboundConnector(OutboundConnectorOne.class)
                .andHandleResponse(
                    (latestResponse, context) -> {
                      List<Integer> valueResponses =
                          context.getOrchestrationStepResponses().stream()
                              .map(step -> step.result().getValue())
                              .toList();
                      Integer sum =
                          IntStream.range(0, valueResponses.size())
                              .map(i -> valueResponses.get(i) * (int) Math.pow(10, i + 1))
                              .sum();
                      context.setAggregatedResponse(
                          new ScenarioResponse("scenario-handled-response", sum),
                          Optional.of(StepResultCloner.forSerializable()));
                    })
                .callOutboundConnector(OutboundConnectorThree.class)
                .withResponseCloner(StepResultCloner.forCopyConstructor())
                .andAggregateResponse(
                    (latestResponse, previousOverallResponse) -> {
                      if (previousOverallResponse.isEmpty()) {
                        return new ScenarioResponse(
                            latestResponse.getId(), latestResponse.getValue() * 1000);
                      }
                      ScenarioResponse previous = previousOverallResponse.get();
                      return new ScenarioResponse(
                          previous.getId(), previous.getValue() + latestResponse.getValue() * 1000);
                    });

            // any unspecified connector (provider) calls any unspecified connector (consumer) and
            // aggregates all responses in a sum
            dsl.forAnyUnspecifiedScenarioProvider()
                .callAnyUnspecifiedScenarioConsumer()
                .andAggregateResponse(
                    (latestResponse, previousOverallResponse) -> {
                      ScenarioResponse response =
                          new ScenarioResponse(
                              "scenario-aggregated-response", latestResponse.getValue());
                      previousOverallResponse.ifPresent(
                          previous -> response.setValue(response.getValue() + previous.getValue()));
                      return response;
                    });
          });
    }
  }

  @InboundConnector(
      connectorId = InboundConnectorOne.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CustomOrchestrationScenario.ID,
      requestModel = String.class,
      responseModel = ScenarioResponse.class)
  public class InboundConnectorOne extends GenericInboundConnectorBase {

    public static final String ID = "inboundConnectorOne";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("dummyInputOne");
    }
  }

  @InboundConnector(
      connectorId = InboundConnectorTwo.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CustomOrchestrationScenario.ID,
      requestModel = String.class,
      responseModel = ScenarioResponse.class)
  public class InboundConnectorTwo extends GenericInboundConnectorBase {

    public static final String ID = "inboundConnectorTwo";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("dummyInputTwo");
    }
  }

  @InboundConnector(
      connectorId = InboundConnectorThree.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CustomOrchestrationScenario.ID,
      requestModel = String.class,
      responseModel = ScenarioResponse.class)
  public class InboundConnectorThree extends GenericInboundConnectorBase {

    public static final String ID = "inboundConnectorThree";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("dummyInputThree");
    }
  }

  @InboundConnector(
      connectorId = InboundConnectorFour.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CustomOrchestrationScenario.ID,
      requestModel = String.class,
      responseModel = ScenarioResponse.class)
  public class InboundConnectorFour extends GenericInboundConnectorBase {

    public static final String ID = "inboundConnectorFour";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("dummyInputFour");
    }
  }

  @OutboundConnector(
      connectorId = OutboundConnectorOne.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CustomOrchestrationScenario.ID,
      requestModel = String.class,
      responseModel = ScenarioResponse.class)
  public class OutboundConnectorOne extends GenericOutboundConnectorBase {

    public static final String ID = "outboundConnectorOne";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("messageConnector1");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.setBody().constant(new ScenarioResponse("testOne", 1)));
    }
  }

  @OutboundConnector(
      connectorId = OutboundConnectorTwo.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CustomOrchestrationScenario.ID,
      requestModel = String.class,
      responseModel = ScenarioResponse.class)
  public class OutboundConnectorTwo extends GenericOutboundConnectorBase {

    public static final String ID = "outboundConnectorTwo";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("messageConnector2");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.setBody().constant(new ScenarioResponse("testTwo", 2)));
    }
  }

  @OutboundConnector(
      connectorId = OutboundConnectorThree.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = CustomOrchestrationScenario.ID,
      requestModel = String.class,
      responseModel = ScenarioResponse.class)
  public class OutboundConnectorThree extends GenericOutboundConnectorBase {

    public static final String ID = "outboundConnectorThree";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("messageConnector3");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.setBody().constant(new ScenarioResponse("testThree", 3)));
    }
  }

  // <-- ORCHESTRATED SCENARIO END

  // <-- AUTO ORCHESTRATED SCENARIO START
  @IntegrationScenario(scenarioId = AutoOrchestratedScenario.ID, requestModel = String.class)
  public class AutoOrchestratedScenario extends IntegrationScenarioBase {

    public static final String ID = "autoOrchestratedScenario";
  }

  @InboundConnector(
      connectorId = AutoOrchestratedInboundConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = AutoOrchestratedScenario.ID,
      requestModel = String.class)
  public class AutoOrchestratedInboundConnector extends GenericInboundConnectorBase {

    public static final String ID = "autoOrchestratedInboundConnector";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("autoOrchestratedInput");
    }
  }

  @OutboundConnector(
      connectorId = AutoOrchestratedOutboundConnectorOne.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = AutoOrchestratedScenario.ID,
      requestModel = String.class)
  public class AutoOrchestratedOutboundConnectorOne extends GenericOutboundConnectorBase {

    public static final String ID = "autoOrchestratedOutboundConnectorOne";

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(
              routeDefinition -> routeDefinition.setBody(e -> e.getIn().getBody() + ID));
    }

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("messageConnector1");
    }
  }

  @OutboundConnector(
      connectorId = AutoOrchestratedOutboundConnectorTwo.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = AutoOrchestratedScenario.ID,
      requestModel = String.class)
  public class AutoOrchestratedOutboundConnectorTwo extends GenericOutboundConnectorBase {

    public static final String ID = "autoOrchestratedOutboundConnectorTwo";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("messageConnector2");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(
              routeDefinition -> routeDefinition.setBody(e -> e.getIn().getBody() + ID));
    }
  }

  // <-- AUTO ORCHESTRATED SCENARIO END
}
