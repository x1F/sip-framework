package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connector.OutboundConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationHandlers;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.model.ProcessorDefinition;

/**
 * Class for generating Camel routes for scenario consumer calls from a DSL
 *
 * <p><em>For internal use only</em>
 */
@Slf4j
@SuppressWarnings("rawtypes")
final class RouteGeneratorForCallScenarioConsumerDefinition<M> extends RouteGeneratorBase {

  private final CallScenarioConsumerBaseDefinition<?, ?, M> definitionElement;

  private final Set<IntegrationScenarioConsumerDefinition> overallUnhandledConsumers;

  @Getter(lazy = true, value = AccessLevel.PROTECTED)
  private final Set<IntegrationScenarioConsumerDefinition> handledConsumers =
      resolveAndVerifyHandledConsumers();

  RouteGeneratorForCallScenarioConsumerDefinition(
      final ScenarioOrchestrationInfo orchestrationInfo,
      final CallScenarioConsumerBaseDefinition<?, ?, M> definitionElement,
      final Set<IntegrationScenarioConsumerDefinition> overallUnhandledConsumers) {
    super(orchestrationInfo);
    this.definitionElement = definitionElement;
    this.overallUnhandledConsumers = overallUnhandledConsumers;
  }

  private Set<IntegrationScenarioConsumerDefinition> resolveAndVerifyHandledConsumers() {
    final var consumers = resolveHandledConsumers();

    // verify that given providers are not already handled
    final var doubleHandledConsumers =
        consumers.stream().filter(handled -> !overallUnhandledConsumers.contains(handled)).toList();
    if (!doubleHandledConsumers.isEmpty()) {
      log.warn(
          "The following consumers are used more than once in orchestration for scenario '{}': {}",
          getIntegrationScenarioId(),
          doubleHandledConsumers.stream()
              .map(obj -> obj.getClass().getName())
              .collect(Collectors.joining(",")));
    }
    return consumers;
  }

  private Set<IntegrationScenarioConsumerDefinition> resolveHandledConsumers() {
    if (definitionElement instanceof CallScenarioConsumerByClassDefinition element) {
      return Collections.singleton(retrieveConsumerFromClassDefinition(element));
    } else if (definitionElement instanceof CallScenarioConsumerByConnectorIdDefinition element) {
      return Collections.singleton(retrieveConsumerFromConnectorIdDefinition(element));
    } else if (definitionElement instanceof CallScenarioConsumerCatchAllDefinition) {
      return Set.copyOf(overallUnhandledConsumers);
    }

    throw SIPFrameworkInitializationException.init(
        "Unhandled scenario-consumer definition subclass: %s",
        definitionElement.getClass().getName());
  }

  private IntegrationScenarioConsumerDefinition retrieveConsumerFromClassDefinition(
      final CallScenarioConsumerByClassDefinition element) {
    return getDeclarationsRegistry().getConsumersForScenario(getIntegrationScenario()).stream()
        .filter(
            outboundConnectorDefinition ->
                element.getConsumerClass().equals(outboundConnectorDefinition.getClass()))
        .findFirst()
        .orElseThrow(
            () ->
                SIPFrameworkInitializationException.init(
                    "Consumer-class '%s' is used on orchestration for integration scenario '%s', but it is not registered with that scenario. Registered consumer classes are %s",
                    element.getConsumerClass().getName(),
                    getIntegrationScenarioId(),
                    getOutboundConnectors().stream()
                        .map(conn -> conn.getClass().getName())
                        .toList()));
  }

  private OutboundConnectorDefinition retrieveConsumerFromConnectorIdDefinition(
      final CallScenarioConsumerByConnectorIdDefinition element) {
    return getOutboundConnectors().stream()
        .filter(
            outboundConnectorDefinition ->
                element.getConnectorId().equals(outboundConnectorDefinition.getId()))
        .findFirst()
        .orElseThrow(
            () ->
                SIPFrameworkInitializationException.init(
                    "Connector ID '%s' is used in orchestration for integration scenario '%s', but it is not registered with that scenario. Registered outbound connectors are %s",
                    element.getConnectorId(),
                    getIntegrationScenarioId(),
                    getOutboundConnectors().stream().map(ConnectorDefinition::getId).toList()));
  }

  private List<OutboundConnectorDefinition> getOutboundConnectors() {
    return getDeclarationsRegistry().getOutboundConnectorsByScenarioId(getIntegrationScenarioId());
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    for (final var consumer : getHandledConsumers()) {
      // prepare request for consumer (as response might still be on the body) and call it
      routeDefinition
          .transform()
          .method(
              ScenarioOrchestrationHandlers.handleRequestToConsumer(
                  consumer, definitionElement.getRequestPreparation()))
          .to(getEndpointForConsumer(consumer));

      // store / aggregate the response and place it on the body
      routeDefinition
          .transform()
          .method(
              ScenarioOrchestrationHandlers.handleResponseFromConsumer(
                  consumer,
                  definitionElement.getStepResultCloner(),
                  definitionElement.getResponseConsumer()));

      overallUnhandledConsumers.remove(consumer);
    }
  }

  private EndpointProducerBuilder getEndpointForConsumer(
      final IntegrationScenarioConsumerDefinition consumer) {
    return Objects.requireNonNull(getOrchestrationInfo().getConsumerEndpoints().get(consumer));
  }
}
