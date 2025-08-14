package one.x1f.sip.foundation.core.apps.declarative.config;

import one.x1f.sip.foundation.core.annotation.SIPIntegrationAdapter;
import one.x1f.sip.foundation.core.declarative.annotation.*;
import one.x1f.sip.foundation.core.declarative.configuration.ConfigurationDefinition;
import one.x1f.sip.foundation.core.declarative.configuration.ConnectorOnExceptionDefinition;
import one.x1f.sip.foundation.core.declarative.connector.GenericInboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.connector.GenericOutboundConnectorBase;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import one.x1f.sip.foundation.core.util.exception.SIPAdapterException;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.RouteConfigurationDefinition;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@SIPIntegrationAdapter
@ComponentScan(excludeFilters = @ComponentScan.Filter(SIPIntegrationAdapter.class))
public class ConfigurationHandlersAdapter {
  public static final String MESSAGE_IN = "in";
  public static final String MESSAGE_OUT = "out";

  public static final String MESSAGE_SCENARIO = "scenario";
  public static final String FAULTY_DIRECT_URI = "trigger-config";
  public static final String CUSTOM_DECLARATIVE_CONFIG = "CustomDeclarativeConfig";
  public static final String SCENARIO_DECLARATIVE_CONFIG = "ScenarioDeclarativeConfig";

  @IntegrationScenario(
      scenarioId = ConfiguredScenario.ID,
      requestModel = String.class,
      responseModel = String.class)
  @ConfigurationHandler({ScenarioDeclarativeConfig.class, CustomDeclarativeConfig.class})
  public class ConfiguredScenario extends IntegrationScenarioBase {
    public static final String ID = "ConfiguredScenario";
  }

  @InboundConnector(
      connectorId = ConfiguredInConnector.ID,
      connectorGroup = "in",
      integrationScenario = ConfiguredScenario.ID,
      requestModel = String.class)
  @ConfigurationHandler(CustomDeclarativeConfig.class)
  public class ConfiguredInConnector extends GenericInboundConnectorBase {
    public static final String ID = "ConfiguredInConnector";

    @Override
    @Deprecated
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(this::defineRequestRoute);
    }

    protected void defineRequestRoute(final RouteDefinition definition) {
      definition.process(
          exchange -> {
            String body = exchange.getMessage().getBody(String.class);
            if (body.equals(MESSAGE_IN)) {
              throw new RuntimeException("test");
            }
            if (body.equals(MESSAGE_SCENARIO)) {
              throw new SIPAdapterException("test");
            }
          });
    }

    @Override
    protected EndpointConsumerBuilder defineInitiatingEndpoint() {
      return StaticEndpointBuilders.direct(FAULTY_DIRECT_URI);
    }

    @ConnectorExceptionHandler(RuntimeException.class)
    public ConnectorOnExceptionDefinition define() {
      return route ->
          route
              .process(
                  exchange ->
                      exchange.getMessage().setBody("Handled in " + ConfiguredInConnector.ID))
              .handled(true);
    }
  }

  @OutboundConnector(
      connectorId = "ConfiguredOutConnector",
      connectorGroup = "out",
      integrationScenario = ConfiguredScenario.ID,
      requestModel = String.class)
  @ConfigurationHandler(CustomDeclarativeConfig.class)
  public class ConfiguredOutConnector extends GenericOutboundConnectorBase {

    @Override
    @Deprecated
    protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
      return ConnectorOrchestrator.forConnector(this)
          .setRequestRouteTransformer(
              definition ->
                  definition.process(
                      exchange -> {
                        if (exchange.getMessage().getBody(String.class).equals(MESSAGE_OUT)) {
                          throw new RuntimeException("test");
                        }
                      }));
    }

    @Override
    protected EndpointProducerBuilder defineOutgoingEndpoint() {
      return StaticEndpointBuilders.log("message");
    }
  }

  @Configuration
  public class CustomDeclarativeConfig implements ConfigurationDefinition {

    @Override
    public OutputDefinition<?> define(RouteConfigurationDefinition definition) {
      return definition
          .onException(RuntimeException.class)
          .process(
              exchange -> exchange.getMessage().setBody("Handled by " + CUSTOM_DECLARATIVE_CONFIG))
          .handled(true);
    }
  }

  @Configuration
  public class ScenarioDeclarativeConfig implements ConfigurationDefinition {

    @Override
    public OutputDefinition<?> define(RouteConfigurationDefinition definition) {
      return definition
          .onException(SIPAdapterException.class)
          .process(
              exchange ->
                  exchange.getMessage().setBody("Handled by " + SCENARIO_DECLARATIVE_CONFIG))
          .handled(true);
    }
  }
}
