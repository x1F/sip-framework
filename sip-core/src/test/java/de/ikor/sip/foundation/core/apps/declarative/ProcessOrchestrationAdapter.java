package de.ikor.sip.foundation.core.apps.declarative;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.declarative.annotation.CompositeProcess;
import de.ikor.sip.foundation.core.declarative.annotation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.annotation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.process.ProcessOrchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessBase;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @Filter(SIPIntegrationAdapter.class))
public class ProcessOrchestrationAdapter {

  private final String GROUP_ID = "group";

  public record PartnerNameRequest(String name) {}

  public record PartnerResponse(Integer id, String name) {}

  @Data
  @AllArgsConstructor
  public static class DebtResponse {

    private String requestedPartnerName;
    private BigDecimal amount;
    private String requestedBy;
  }

  @IntegrationScenario(
      scenarioId = getPartnerByName.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = PartnerResponse.class)
  public class getPartnerByName extends IntegrationScenarioBase {

    public static final String ID = "getPartnerByName";
  }

  @IntegrationScenario(
      scenarioId = getPartnerDebtById.ID,
      requestModel = Integer.class,
      responseModel = DebtResponse.class)
  public class getPartnerDebtById extends IntegrationScenarioBase {

    public static final String ID = "getPartnerDebtById";

    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
      return ScenarioOrchestrator.forOrchestrationDslWithResponse(
          DebtResponse.class,
          dsl -> {
            dsl.forInboundConnectors(getPartnerDebtByIdInConnector.class)
                .callOutboundConnector(getPartnerDebtByIdOutConnector.class)
                .andHandleResponse(
                    (latestResponse, context) -> latestResponse.setRequestedBy("Front-end"));
            dsl.forScenarioProviders(GetCustomerDebtByNameOrchestrator.class)
                .callOutboundConnector(getPartnerDebtByIdOutConnector.class)
                .andHandleResponse(
                    (latestResponse, context) ->
                        latestResponse.setRequestedBy("Process Orchestrator"));
          });
    }
  }

  @IntegrationScenario(
      scenarioId = getPartnerDebtByName.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = DebtResponse.class)
  public class getPartnerDebtByName extends IntegrationScenarioBase {

    public static final String ID = "getPartnerDebtByName";

    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
      return ScenarioOrchestrator.forOrchestrationDslWithResponse(
          DebtResponse.class,
          dsl -> {
            dsl.forAnyUnspecifiedScenarioProvider()
                .callScenarioConsumer(GetPartnerDebtByNameOutLogConnector.class)
                .withRequestPreparation(
                    request -> {
                      PartnerNameRequest originalRequest =
                          request.getOriginalRequest(PartnerNameRequest.class);
                      return new PartnerNameRequest(originalRequest.name + "-LOG THIS");
                    })
                .andNoResponseHandling()
                .callScenarioConsumer(GetCustomerDebtByNameOrchestrator.class)
                .andNoResponseHandling();
          });
    }
  }

  @CompositeProcess(
      processId = GetCustomerDebtByNameOrchestrator.ID,
      provider = getPartnerDebtByName.class,
      consumers = {getPartnerDebtById.class, getPartnerByName.class})
  public class GetCustomerDebtByNameOrchestrator extends CompositeProcessBase {

    private static final String ID = "GetCustomerDebtByNameOrchestrator";

    @Override
    public Orchestrator<CompositeProcessOrchestrationInfo> getOrchestrator() {
      return ProcessOrchestrator.forOrchestrationDsl(
          dsl -> {
            dsl.callConsumer(getPartnerByName.class)
                .withNoResponseHandling()
                .callConsumer(getPartnerDebtById.class)
                .withRequestPreparation(
                    context -> {
                      PartnerResponse response =
                          context.getLatestResponse(PartnerResponse.class).get();
                      return response.id;
                    })
                .withResponseHandling(
                    (latestResponse, context) -> {
                      PartnerNameRequest originalRequest =
                          context.getOriginalRequest(PartnerNameRequest.class);
                      DebtResponse debtByIdResponse =
                          (DebtResponse)
                              context
                                  .getResultOfFirstStepFromConsumer(getPartnerDebtById.class)
                                  .get()
                                  .response();
                      DebtResponse debtResponse =
                          new DebtResponse(
                              originalRequest.name,
                              debtByIdResponse.amount,
                              debtByIdResponse.requestedBy);
                      context.setProcessResponse(debtResponse, Optional.empty());
                    });
          });
    }
  }

  @InboundConnector(
      connectorId = GetPartnerByNameInConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = getPartnerByName.ID,
      requestModel = String.class,
      responseModel = PartnerResponse.class)
  public class GetPartnerByNameInConnector extends GenericInboundConnectorBase {

    public static final String ID = "GetPartnerByNameInConnector";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("GetPartnerByNameInConnector");
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
      connectorId = GetPartnerByNameOutConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = getPartnerByName.ID,
      requestModel = PartnerNameRequest.class,
      responseModel = PartnerResponse.class)
  public class GetPartnerByNameOutConnector extends GenericOutboundConnectorBase {

    public static final String ID = "getPartnerByNameOutConnector";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("getPartnerByNameOutConnector").plain(true);
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
      connectorId = getPartnerDebtByIdInConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = getPartnerDebtById.ID,
      requestModel = String.class,
      responseModel = DebtResponse.class)
  public class getPartnerDebtByIdInConnector extends GenericInboundConnectorBase {

    public static final String ID = "getPartnerDebtByIdInConnector";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("getPartnerDebtByIdInConnector");
    }
  }

  @OutboundConnector(
      connectorId = getPartnerDebtByIdOutConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = getPartnerDebtById.ID,
      requestModel = Integer.class,
      responseModel = DebtResponse.class)
  public class getPartnerDebtByIdOutConnector extends GenericOutboundConnectorBase {

    public static final String ID = "getPartnerDebtByOutConnector";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("getPartnerDebtByOutConnector").plain(true);
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              routeDefinition ->
                  routeDefinition.process(
                      e -> {
                        e.getIn()
                            .setBody(new DebtResponse(null, new BigDecimal("100000.00"), null));
                      }));
    }
  }

  @InboundConnector(
      connectorId = GetPartnerDebtByNameInConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = getPartnerDebtByName.ID,
      requestModel = String.class,
      responseModel = PartnerResponse.class)
  public class GetPartnerDebtByNameInConnector extends GenericInboundConnectorBase {

    public static final String ID = "GetPartnerDebtByNameInConnector";

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("GetPartnerDebtByNameInConnector");
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
      connectorId = GetPartnerDebtByNameOutLogConnector.ID,
      connectorGroup = GROUP_ID,
      integrationScenario = getPartnerDebtByName.ID,
      requestModel = PartnerNameRequest.class)
  public class GetPartnerDebtByNameOutLogConnector extends GenericOutboundConnectorBase {

    public static final String ID = "getPartnerDebtByNameOutLogConnector";

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("getPartnerDebtByNameOutLogConnector").plain(true);
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setResponseRouteTransformer(
              def -> {
                def.setBody(body -> new DebtResponse("partner", BigDecimal.ZERO, "log"));
              });
    }
  }
}
