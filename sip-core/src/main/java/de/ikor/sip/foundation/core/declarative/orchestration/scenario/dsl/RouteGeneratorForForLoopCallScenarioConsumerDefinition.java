package de.ikor.sip.foundation.core.declarative.orchestration.scenario.dsl;

import de.ikor.sip.foundation.core.declarative.orchestration.process.routebuilding.ScenarioStepIterations;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationHelper;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.support.ExpressionAdapter;

@SuppressWarnings("rawtypes")
@Slf4j
final class RouteGeneratorForForLoopCallScenarioConsumerDefinition<M> extends RouteGeneratorBase {
  private final ForLoopCallScenarioConsumerDefinition<?, M> loopDefinition;

  private final Set<IntegrationScenarioConsumerDefinition> overallUnhandledConsumers;

  RouteGeneratorForForLoopCallScenarioConsumerDefinition(
      final ScenarioOrchestrationInfo orchestrationInfo,
      final ForLoopCallScenarioConsumerDefinition<?, M> loopDefinition,
      final Set<IntegrationScenarioConsumerDefinition> overallUnhandledConsumers) {
    super(orchestrationInfo);
    this.loopDefinition = loopDefinition;
    this.overallUnhandledConsumers = overallUnhandledConsumers;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    if (loopDefinition.getLoopStatements().isEmpty()) {
      throw SIPFrameworkInitializationException.init(
          "Empty forLoop statement attached in orchestration for integration-scenario %s",
          getIntegrationScenarioId());
    }

    loopDefinition
        .getLoopStatements()
        .forEach(
            withCounter(
                (i, branchStatements) -> {
                  if (branchStatements.statements().isEmpty()) {
                    log.warn(
                        "Orchestration for integration-scenario {} contains a forLoop-statement that does not specify any actions in branch #{}",
                        getIntegrationScenarioId(),
                        i + 1);
                  }

                  final var loopDef =
                      routeDefinition.loop(
                          new ForLoopIterationsExpression(branchStatements.predicate()));
                  branchStatements
                      .statements()
                      .forEach(statement -> buildRouteForStatement(loopDef, statement));
                  loopDef.end();
                }));
  }

  private static <T> Consumer<T> withCounter(BiConsumer<Integer, T> consumer) {
    AtomicInteger counter = new AtomicInteger(0);
    return item -> consumer.accept(counter.getAndIncrement(), item);
  }

  @RequiredArgsConstructor
  static class ForLoopIterationsExpression extends ExpressionAdapter {
    private final ScenarioStepIterations predicate;

    @Override
    public Object evaluate(Exchange exchange) {
      return handleIterations(exchange, predicate);
    }

    private int handleIterations(final Exchange exchange, final ScenarioStepIterations iterations) {
      return new IterationsHandler(iterations).executeIterations(exchange);
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class IterationsHandler {
    private final ScenarioStepIterations iterations;

    @Handler
    public int executeIterations(final Exchange exchange) {
      final ScenarioOrchestrationContext context = retrieveOrchestrationContext(exchange);
      return iterations.determineIterations(context);
    }

    private ScenarioOrchestrationContext retrieveOrchestrationContext(final Exchange exchange) {
      final var context =
          Objects.requireNonNull(
              exchange.getProperty(
                  ScenarioOrchestrationContext.PROPERTY_NAME, ScenarioOrchestrationContext.class),
              "Orchestration context for scenario-orchestration could not be retrieved from exchange");
      ScenarioOrchestrationHelper.setExchange(context, exchange);
      return context;
    }
  }

  @SuppressWarnings("unchecked")
  private <T extends ProcessorDefinition<T>> void buildRouteForStatement(
      final T routeDefinition, final CallableWithinProviderDefinition statement) {
    if (statement instanceof CallScenarioConsumerBaseDefinition callDef) {
      new RouteGeneratorForCallScenarioConsumerDefinition<>(
              getOrchestrationInfo(), callDef, overallUnhandledConsumers)
          .generateRoute(routeDefinition);
    } else {
      throw SIPFrameworkInitializationException.init(
          "Unhandled statement type '%s' used in forLoop-branch of orchestration for integration-scenario %s",
          statement.getClass().getName(), getIntegrationScenarioId());
    }
  }
}
