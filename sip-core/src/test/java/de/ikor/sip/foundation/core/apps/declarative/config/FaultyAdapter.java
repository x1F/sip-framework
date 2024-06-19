package de.ikor.sip.foundation.core.apps.declarative.config;

import de.ikor.sip.foundation.core.annotation.SIPIntegrationAdapter;
import de.ikor.sip.foundation.core.declarative.annonation.*;
import de.ikor.sip.foundation.core.declarative.configuration.DeclarativeConfigurationDefinition;
import de.ikor.sip.foundation.core.declarative.configuration.DeclarativeOnExceptionDefinition;
import de.ikor.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import de.ikor.sip.foundation.core.util.exception.SIPAdapterException;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.RouteConfigurationDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @ComponentScan.Filter(SIPIntegrationAdapter.class))
public class FaultyAdapter {
  public static final String MESSAGE_IN = "in";
  public static final String MESSAGE_OUT = "out";
  public static final String FAULTY_DIRECT_URI = "trigger-config";

  @IntegrationScenario(
      scenarioId = ConfiguredScenario.ID,
      requestModel = String.class,
      responseModel = String.class)
  public class ConfiguredScenario extends IntegrationScenarioBase {
    public static final String ID = "ConfiguredScenario";
  }

  @InboundConnector(
      connectorId = "ConfiguredInConnector",
      connectorGroup = "in",
      integrationScenario = ConfiguredScenario.ID,
      requestModel = String.class)
  @DeclarativeConfiguration(configurations = CustomDeclarativeConfig.class)
  public class ConfiguredInConnector extends GenericInboundConnectorBase {

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(this::defineRequestRoute);
    }

    protected void defineRequestRoute(final RouteDefinition definition) {
      definition.process(
          exchange -> {
            if (exchange.getMessage().getBody(String.class).equals(MESSAGE_IN)) {
              throw new SIPAdapterException("test");
            }
          });
    }

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct(FAULTY_DIRECT_URI);
    }

    @ConnectorErrorHandler(exceptions = SIPAdapterException.class)
    public DeclarativeOnExceptionDefinition define() {
      return route ->
          route
              .process(exchange -> exchange.getMessage().setBody("Handled in Connector"))
              .handled(true);
    }
  }

  @OutboundConnector(
      connectorId = "ConfiguredOutConnector",
      connectorGroup = "out",
      integrationScenario = ConfiguredScenario.ID,
      requestModel = String.class)
  @DeclarativeConfiguration(configurations = CustomDeclarativeConfig.class)
  public class ConfiguredOutConnector extends GenericOutboundConnectorBase {

    @Override
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(
              definition ->
                  definition.process(
                      exchange -> {
                        if (exchange.getMessage().getBody(String.class).equals(MESSAGE_OUT)) {
                          throw new SIPAdapterException("test");
                        }
                      }));
    }

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }
  }

  @Configuration
  public class CustomDeclarativeConfig implements DeclarativeConfigurationDefinition {

    @Override
    public void define(RouteConfigurationDefinition definition) {
      definition
          .onException(SIPAdapterException.class)
          .process(exchange -> exchange.getMessage().setBody("Handled by CustomDeclarativeConfig"))
          .handled(true);
    }
  }
}
