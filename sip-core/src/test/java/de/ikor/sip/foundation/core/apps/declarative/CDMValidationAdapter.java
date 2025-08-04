package de.ikor.sip.foundation.core.apps.declarative;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.declarative.annotation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.annotation.OutboundConnector;
import de.ikor.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @Filter(SIPIntegrationAdapter.class))
public class CDMValidationAdapter {

  @Data
  @AllArgsConstructor
  public static class CDMRequest {
    private int id;
  }

  @Data
  @AllArgsConstructor
  public static class CDMResponse {
    private String id;
  }

  @IntegrationScenario(
      scenarioId = CDMValidationScenario.ID,
      requestModel = CDMRequest.class,
      responseModel = CDMResponse.class)
  public class CDMValidationScenario extends IntegrationScenarioBase {
    public static final String ID = "CDMValidation";
  }

  @InboundConnector(
      connectorGroup = "SIP1",
      integrationScenario = CDMValidationScenario.ID,
      requestModel = CDMRequest.class,
      responseModel = CDMResponse.class)
  public class InboundCDMConnector extends GenericInboundConnectorBase {

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct("cdm-validator");
    }
  }

  @OutboundConnector(
      connectorGroup = "SIP2",
      integrationScenario = CDMValidationScenario.ID,
      requestModel = CDMRequest.class,
      responseModel = CDMResponse.class)
  public class OutboundCDMConnector extends GenericOutboundConnectorBase {
    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(this::configureEndpointRoute);
    }

    protected void configureEndpointRoute(RouteDefinition definition) {
      definition.process(
          exchange -> {
            CDMRequest request = exchange.getMessage().getBody(CDMRequest.class);
            CDMResponse response = new CDMResponse("ID: " + request.getId());
            exchange.getMessage().setBody(request.getId() == 1000 ? response : "Wrong CDM type");
          });
    }
  }
}
