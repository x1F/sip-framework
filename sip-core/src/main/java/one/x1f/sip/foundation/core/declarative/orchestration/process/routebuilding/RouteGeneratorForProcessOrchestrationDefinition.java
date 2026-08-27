package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationHandlers;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.*;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;

/**
 * Class for generating Camel routes for process orchestration via DSL This class generates route
 * starts and delegate creating routes for consumer calls and conditionals to other route
 * generators.
 *
 * <p><em>For internal use only</em>
 */
@Slf4j
public final class RouteGeneratorForProcessOrchestrationDefinition extends RouteGeneratorProcessBase
    implements Runnable {

  private final ProcessOrchestrationDefinition processOrchestrationDefinition;

  @Getter(lazy = true)
  private final IntegrationScenarioDefinition orchestrationProvider =
      getDeclarationsRegistry().getCompositeProcessProviderDefinition(getCompositeProcessId());

  public RouteGeneratorForProcessOrchestrationDefinition(
      final CompositeProcessOrchestrationInfo orchestrationInfo,
      final ProcessOrchestrationDefinition processOrchestrationDefinition) {
    super(orchestrationInfo);
    this.processOrchestrationDefinition = processOrchestrationDefinition;
  }

  @Override
  public void run() {
    this.generateRoutes(getRoutesDefinition());
  }

  void generateRoutes(final RoutesDefinition routesDefinition) {

    if (this.getOrchestrationProvider() == null) {
      throw SIPFrameworkInitializationException.init(
          "Orchestration for composite process '%s' doesn't have a provider. Please define it.",
          getCompositeProcessId());
    }

    final var unhandledProcessConsumers =
        new HashSet<>(getOrchestrationInfo().getConsumerEndpoints().keySet());

    final var routeDef = generateRouteStart(routesDefinition);

    routeDef.bean(
        CompositeProcessOrchestrationHandlers.handleContextInitialization(getCompositeProcess()));

    for (final var element :
        RouteGeneratorInternalHelper.getConsumerCalls(processOrchestrationDefinition)) {
      if (element instanceof CallProcessConsumer<?, ?> ele) {
        new RouteGeneratorForCallProcessConsumer(
                getOrchestrationInfo(), ele, unhandledProcessConsumers)
            .generateRoute(routeDef);
      } else if (element instanceof CallNestedCondition<?> ele) {
        new RouteGeneratorForCallConditionalProcessConsumer(
                getOrchestrationInfo(), ele, unhandledProcessConsumers)
            .generateRoute(routeDef);
      } else if (element instanceof CallWhileLoopStatement<?> ele) {
        new RouteGeneratorForWhileLoopProcessConsumer(
                getOrchestrationInfo(), ele, unhandledProcessConsumers)
            .generateRoute(routeDef);
      } else if (element instanceof CallForLoopStatement<?> ele) {
        new RouteGeneratorForForLoopProcessConsumer(
                getOrchestrationInfo(), ele, unhandledProcessConsumers)
            .generateRoute(routeDef);
      } else if (element instanceof CallSplitStatement<?> ele) {
        new RouteGeneratorForSplitProcessConsumer(
                getOrchestrationInfo(), ele, unhandledProcessConsumers)
            .generateRoute(routeDef);
      } else if (element instanceof CallProcess<?> ele) {
        new RouteGeneratorForProcessTransformer(getOrchestrationInfo(), ele)
            .generateRoute(routeDef);
      }
    }

    routeDef.bean(CompositeProcessOrchestrationHandlers.handleErrorThrownIfNoConsumerWasCalled());

    if (!unhandledProcessConsumers.isEmpty()) {
      log.warn(
          "Orchestration for composite process '{}' does not call consumers '{}' for calls coming in from '{}'. Consider removing the consumer from the process definition.",
          getCompositeProcessId(),
          unhandledProcessConsumers.stream()
              .map(consumer -> consumer.getClass().getSimpleName())
              .collect(Collectors.joining(",")),
          this.getOrchestrationProvider().getClass().getSimpleName());
    }
  }

  private RouteDefinition generateRouteStart(final RoutesDefinition routesDefinition) {
    final IntegrationScenarioDefinition provider = this.getOrchestrationProvider();
    final var providerIds = provider.getClass().getSimpleName();
    final var orchestrationRouteId =
        getRoutesRegistry()
            .generateRouteIdForCompositeScenarioOrchestrator(getCompositeProcess(), providerIds);
    return routesDefinition.from(getProviderCamelEndpoint(provider)).routeId(orchestrationRouteId);
  }

  private EndpointConsumerBuilder getProviderCamelEndpoint(
      final IntegrationScenarioDefinition provider) {
    return Objects.requireNonNull(getOrchestrationInfo().getProviderEndpoints().get(provider));
  }
}
