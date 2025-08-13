package one.x1f.sip.foundation.core.apps.declarative;

import static one.x1f.sip.foundation.core.declarative.orchestration.process.ProcessOrchestrationContextPredicates.*;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
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
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessBase;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @Filter(SIPIntegrationAdapter.class))
public class ProcessOrchestrationConditionalAdapter {

  private final String GROUP_ID = "conditional_group";

  public record PartnerNameRequest(String name) {}

  public record PartnerResponse(Integer id, String name) {}

  @Data
  @AllArgsConstructor
  public static class DebtResponse {

    private BigDecimal amount;
    private String requestedBy;
  }

  @IntegrationScenario(
      scenarioId = ConditionalGetPartnerByName.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = PartnerResponse.class)
  public class ConditionalGetPartnerByName extends IntegrationScenarioBase {

    public static final String ID = "getPartnerByNameCond";
  }

  @IntegrationScenario(
      scenarioId = ConditionalGetPartnerByCode.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = PartnerResponse.class)
  public class ConditionalGetPartnerByCode extends IntegrationScenarioBase {

    public static final String ID = "getPartnerByCodeCond";
  }

  @IntegrationScenario(
      scenarioId = LoggingScenario.ID,
      requestModel = DebtResponse.class,
      responseModel = DebtResponse.class)
  public class LoggingScenario extends IntegrationScenarioBase {

    public static final String ID = "logging-scenario";
  }

  @IntegrationScenario(
      scenarioId = ConditionalGetPartnerDebtById.ID,
      requestModel = Integer.class,
      responseModel = DebtResponse.class)
  public class ConditionalGetPartnerDebtById extends IntegrationScenarioBase {

    public static final String ID = "getPartnerDebtByIdCond";

    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
      return ScenarioOrchestrator.forOrchestrationDslWithResponse(
          DebtResponse.class,
          dsl -> {
            dsl.forInboundConnectors(ConditionalGetPartnerDebtByIdInConnector.class)
                .ifCase(c -> true)
                .callOutboundConnector(ConditionalGetPartnerDebtByOutConnector.class)
                .andHandleResponse(
                    (latestResponse, context) -> latestResponse.setRequestedBy("Front-end"));
            dsl.forScenarioProviders(ConditionalGetCustomerDebtByNameOrchestrator.class)
                .callOutboundConnector(ConditionalGetPartnerDebtByOutConnector.class)
                .andHandleResponse(
                    (latestResponse, context) ->
                        latestResponse.setRequestedBy("Process Orchestrator"));
          });
    }
  }

  @IntegrationScenario(
      scenarioId = ConditionalGetPartnerDebtByName.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = DebtResponse.class)
  public class ConditionalGetPartnerDebtByName extends IntegrationScenarioBase {

    public static final String ID = "getPartnerDebtByNameCond";

    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
      return ScenarioOrchestrator.forOrchestrationDslWithResponse(
          DebtResponse.class,
          dsl -> {
            dsl.forAnyUnspecifiedScenarioProvider()
                .callScenarioConsumer(ConditionalGetPartnerDebtByNameOutLogConnector.class)
                .withRequestPreparation(
                    request -> {
                      PartnerNameRequest originalRequest =
                          request.getOriginalRequest(PartnerNameRequest.class);
                      return new PartnerNameRequest(originalRequest.name + "-LOG THIS");
                    })
                .andNoResponseHandling()
                .callScenarioConsumer(ConditionalGetCustomerDebtByNameOrchestrator.class)
                .andNoResponseHandling();
          });
    }
  }

  @CompositeProcess(
      processId = ConditionalGetCustomerDebtByNameOrchestrator.ID,
      provider = ConditionalGetPartnerDebtByName.class,
      consumers = {
        ConditionalGetPartnerDebtById.class,
        ConditionalGetPartnerByName.class,
        ConditionalGetPartnerByCode.class,
        LoggingScenario.class
      })
  public class ConditionalGetCustomerDebtByNameOrchestrator extends CompositeProcessBase {

    private static final String ID = "GetCustomerDebtByNameOrchestratorCond";

    @Override
    public Orchestrator<CompositeProcessOrchestrationInfo> getOrchestrator() {
      return ProcessOrchestrator.forOrchestrationDsl(
          dsl -> {
            dsl.ifCase(headerEquals("partner-name", "any", String.class))
                .callConsumer(ConditionalGetPartnerByCode.class)
                .withNoResponseHandling()
                .elseIfCase(hasHeader("partner-name"))
                .callConsumer(ConditionalGetPartnerByName.class)
                .withNoResponseHandling()
                .elseCase()
                .callConsumer(ConditionalGetPartnerByName.class)
                .withNoResponseHandling()
                .endCases()
                .callConsumer(ConditionalGetPartnerDebtById.class)
                .withRequestPreparation(
                    context -> {
                      PartnerResponse response =
                          (PartnerResponse) context.getLatestResponse().get();
                      return response.id;
                    })
                .withNoResponseHandling()
                .ifCase(
                    responseMatches(
                        o -> {
                          System.out.println(o);
                          return o instanceof DebtResponse;
                        }))
                .callConsumer(LoggingScenario.class)
                .withRequestPreparation(
                    context -> {
                      System.out.println();
                      return context.getLatestResponse().get();
                    })
                .withNoResponseHandling()
                .endCases()
                .ifCase(
                    originalRequestMatches(
                        o ->
                            o instanceof PartnerNameRequest partnerNameRequest
                                && partnerNameRequest.name().equals("a name")))
                .callConsumer(LoggingScenario.class)
                .withRequestPreparation(context -> context.getLatestResponse().get())
                .withResponseHandling(
                    ((latestResponse, context) -> {
                      if (latestResponse instanceof DebtResponse r) {
                        r.setAmount(r.getAmount().add(new BigDecimal(1)));
                      }
                    }))
                .endCases()
                .ifCase(hasHeader(""))
                .endCases();
          });
    }
  }

  @InboundConnector(
      connectorId = ConditionalGetPartnerByNameInConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = ConditionalGetPartnerByName.ID,
      requestModel = String.class,
      responseModel = PartnerResponse.class)
  public class ConditionalGetPartnerByNameInConnector extends GenericInboundConnectorBase {

    public static final String ID = "ConditionalGetPartnerByNameInConnectorCond";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("ConditionalGetPartnerByNameInConnectorCond");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        String partnerName = e.getIn().getBody(String.class);
                        e.getIn().setBody(new PartnerNameRequest(partnerName));
                      }));
    }
  }

  @OutboundConnector(
      connectorId = ConditionalGetPartnerByNameOutConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = ConditionalGetPartnerByName.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = PartnerResponse.class)
  public class ConditionalGetPartnerByNameOutConnector extends GenericOutboundConnectorBase {

    public static final String ID = "getPartnerByNameOutConnectorCond";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("getPartnerByNameOutConnectorCond").plain(true);
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        e.getIn()
                            .setBody(
                                new PartnerResponse(
                                    1, e.getIn().getBody(PartnerNameRequest.class).name()));
                      }));
    }
  }

  @InboundConnector(
      connectorId = ConditionalGetPartnerDebtByIdInConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = ConditionalGetPartnerDebtById.ID,
      requestModel = String.class,
      responseModel = DebtResponse.class)
  public class ConditionalGetPartnerDebtByIdInConnector extends GenericInboundConnectorBase {

    public static final String ID = "getPartnerDebtByIdInConnectorCond";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("getPartnerDebtByIdInConnectorCond");
    }
  }

  @OutboundConnector(
      connectorId = ConditionalGetPartnerDebtByOutConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = ConditionalGetPartnerDebtById.ID,
      requestModel = Integer.class,
      responseModel = DebtResponse.class)
  public class ConditionalGetPartnerDebtByOutConnector extends GenericOutboundConnectorBase {

    public static final String ID = "getPartnerDebtByOutConnectorCond";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("getPartnerDebtByOutConnectorCond").plain(true);
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        e.getIn().setBody(new DebtResponse(new BigDecimal("100000.00"), null));
                      }));
    }
  }

  @InboundConnector(
      connectorId = ConditionalGetPartnerDebtByNameInConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = ConditionalGetPartnerDebtByName.ID,
      requestModel = String.class,
      responseModel = PartnerResponse.class)
  public class ConditionalGetPartnerDebtByNameInConnector extends GenericInboundConnectorBase {

    public static final String ID = "GetPartnerDebtByNameInConnectorCond";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("GetPartnerDebtByNameInConnectorCond");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        String partnerName = e.getIn().getBody(String.class);
                        e.getIn().setBody(new PartnerNameRequest(partnerName));
                      }));
    }
  }

  @OutboundConnector(
      connectorId = ConditionalGetPartnerDebtByNameOutLogConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = ConditionalGetPartnerDebtByName.ID,
      requestModel = PartnerNameRequest.class)
  public class ConditionalGetPartnerDebtByNameOutLogConnector extends GenericOutboundConnectorBase {

    public static final String ID = "getPartnerDebtByNameOutLogConnectorCond";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("getPartnerDebtByNameOutLogConnectorCond").plain(true);
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              def -> {
                def.setBody(body -> new DebtResponse(BigDecimal.ZERO, "log"));
              });
    }
  }

  @InboundConnector(
      connectorId = ConditionalGetPartnerByCodeInConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = ConditionalGetPartnerByCode.ID,
      requestModel = String.class,
      responseModel = PartnerResponse.class)
  public class ConditionalGetPartnerByCodeInConnector extends GenericInboundConnectorBase {

    public static final String ID = "GetPartnerByCodeInConnectorCond";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("GetPartnerByCodeInConnectorCond");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        String partnerName = e.getIn().getBody(String.class);
                        e.getIn().setBody(new PartnerNameRequest(partnerName));
                      }));
    }
  }

  @OutboundConnector(
      connectorId = ConditionalGetPartnerByCodeOutConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = ConditionalGetPartnerByCode.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = PartnerResponse.class)
  public class ConditionalGetPartnerByCodeOutConnector extends GenericOutboundConnectorBase {

    public static final String ID = "getPartnerByCodeOutConnectorCond";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("getPartnerByCodeOutConnectorCond").plain(true);
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        e.getIn()
                            .setBody(
                                new PartnerResponse(
                                    2,
                                    e.getIn().getBody(PartnerNameRequest.class).name() + "Code"));
                      }));
    }
  }

  @InboundConnector(
      connectorGroup = GROUP_ID,
      integrationScenario = LoggingScenario.ID,
      requestModel = DebtResponse.class,
      responseModel = DebtResponse.class)
  public class LoggingInboundConnector extends GenericInboundConnectorBase {

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("Logging");
    }
  }

  @OutboundConnector(
      connectorId = "out-logging-connector",
      connectorGroup = GROUP_ID,
      integrationScenario = LoggingScenario.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = PartnerNameRequest.class)
  public class LoggingOutboundConnector extends GenericOutboundConnectorBase {

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("Logging").plain(true);
    }
  }
}
