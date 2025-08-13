package one.x1f.sip.foundation.core.apps.declarative;

import static java.util.function.Predicate.not;
import static one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl.ScenarioContextPredicates.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.Data;
import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.annotation.OutboundConnector;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl.ScenarioOrchestrationDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @ComponentScan.Filter(SIPIntegrationAdapter.class))
public class ScenarioOrchestratedWithConditionsAdapter {

  public static final String HEADER_MODE = "headerMode";

  @Data
  public static class OrchestratedResponseModel {
    List<String> calledConsumers = new ArrayList<>();
    boolean headerMode = false;
  }

  @IntegrationScenario(
      scenarioId = OrchestratedScenario.ID,
      requestModel = String.class,
      responseModel = OrchestratedResponseModel.class)
  public class OrchestratedScenario extends IntegrationScenarioBase {
    public static final String ID = "OrchestratedScenario";

    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
      return ScenarioOrchestrator.forOrchestrationDslWithResponse(
          OrchestratedResponseModel.class, this::dslDefinition);
    }

    private void dslDefinition(
        final ScenarioOrchestrationDefinition<OrchestratedResponseModel> dsl) {
      dsl.forAnyUnspecifiedScenarioProvider()
          .ifCase(originalRequestMatches(String.class, str -> str.equals("one")))
          .callOutboundConnector(OrchestratedOutboundConnectorOne.class)
          .andAggregateResponse((latest, aggregated) -> latest)
          .elseIfCase(not(ctx -> ctx.getBody(String.class).get().isBlank()))
          .callOutboundConnector(OrchestratedOutboundConnectorTwo.class)
          .andAggregateResponse((latest, aggregated) -> latest)
          .endCases()
          .ifCase(headerEquals(HEADER_MODE, true))
          .callOutboundConnector(OrchestratedOutboundConnectorHeaderMode.class)
          .andAggregateResponse(
              (latest, previous) -> {
                previous.ifPresent(p -> latest.getCalledConsumers().addAll(p.getCalledConsumers()));
                return latest;
              })
          .elseIfCase(responseMatches(resp -> resp.calledConsumers.isEmpty())) // empty on purpose
          .elseIfCase(ctx -> ctx.getResponse().isEmpty()) // also empty on purpose
          .elseCase()
          .callOutboundConnector(OrchestratedOutboundConnectorLog.class)
          .andNoResponseHandling();
    }
  }

  @InboundConnector(
      integrationScenario = OrchestratedScenario.ID,
      connectorGroup = "orchestration",
      requestModel = String.class,
      responseModel = OrchestratedResponseModel.class)
  public class OrchestratedRestInboundConnector extends GenericInboundConnectorBase {
    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("orchestrate");
    }
  }

  @OutboundConnector(
      integrationScenario = OrchestratedScenario.ID,
      requestModel = String.class,
      responseModel = OrchestratedResponseModel.class,
      connectorGroup = "orchestration-one")
  public class OrchestratedOutboundConnectorOne extends GenericOutboundConnectorBase {

    public static final String MOCK_BEAN = "OrchestratedAdapter-MockOne";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.bean(MOCK_BEAN);
    }

    @Bean(MOCK_BEAN)
    private Supplier<OrchestratedResponseModel> mockBean() {
      final var response = new OrchestratedResponseModel();
      response.getCalledConsumers().add(getClass().getSimpleName());
      return () -> response;
    }
  }

  @OutboundConnector(
      integrationScenario = OrchestratedScenario.ID,
      requestModel = String.class,
      responseModel = OrchestratedResponseModel.class,
      connectorGroup = "orchestration-two")
  public class OrchestratedOutboundConnectorTwo extends GenericOutboundConnectorBase {

    public static final String MOCK_BEAN = "OrchestratedAdapter-MockTwo";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.bean(MOCK_BEAN);
    }

    @Bean(MOCK_BEAN)
    private Supplier<OrchestratedResponseModel> mockBean() {
      return () -> {
        final var response = new OrchestratedResponseModel();
        response.getCalledConsumers().add(getClass().getSimpleName());
        return response;
      };
    }
  }

  @OutboundConnector(
      integrationScenario = OrchestratedScenario.ID,
      requestModel = String.class,
      responseModel = OrchestratedResponseModel.class,
      connectorGroup = "orchestration-headermode")
  public class OrchestratedOutboundConnectorHeaderMode extends GenericOutboundConnectorBase {

    public static final String MOCK_BEAN = "OrchestratedAdapter-MockHeaderMode";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.bean(MOCK_BEAN);
    }

    @Bean(MOCK_BEAN)
    private Supplier<OrchestratedResponseModel> mockBean() {
      return () -> {
        final var response = new OrchestratedResponseModel();
        response.getCalledConsumers().add(getClass().getSimpleName());
        response.setHeaderMode(true);
        return response;
      };
    }
  }

  @OutboundConnector(
      integrationScenario = OrchestratedScenario.ID,
      requestModel = String.class,
      connectorGroup = "orchestration-log")
  public class OrchestratedOutboundConnectorLog extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("out");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              def -> {
                def.setBody(body -> new OrchestratedResponseModel());
              });
    }
  }
}
