package de.ikor.sip.foundation.core.declarative.orchestration.scenario.dsl;

import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationHandlers;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;

@SuppressWarnings("rawtypes")
@Slf4j
final class RouteGeneratorForWhileLoopCallScenarioConsumerDefinition<M> extends RouteGeneratorBase {
  private final WhileLoopCallScenarioConsumerDefinition<?, M> loopDefinition;

  private final Set<IntegrationScenarioConsumerDefinition> overallUnhandledConsumers;

  RouteGeneratorForWhileLoopCallScenarioConsumerDefinition(
      final ScenarioOrchestrationInfo orchestrationInfo,
      final WhileLoopCallScenarioConsumerDefinition<?, M> loopDefinition,
      final Set<IntegrationScenarioConsumerDefinition> overallUnhandledConsumers) {
    super(orchestrationInfo);
    this.loopDefinition = loopDefinition;
    this.overallUnhandledConsumers = overallUnhandledConsumers;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    if (loopDefinition.getLoopStatements().isEmpty()) {
      SIPFrameworkInitializationException.init(
          "Empty conditional statement attached in orchestration for integration-scenario %s",
          getIntegrationScenarioId());
    }

    for (final var branch : loopDefinition.getLoopStatements()) {
      if (branch.statements().isEmpty()) {

        var branchIndex = loopDefinition.getLoopStatements().indexOf(branch) + 1;
        log.warn(
            "Orchestration for integration-scenario {} contains a conditional-statement that does not specify any actions in branch #{}",
            getIntegrationScenarioId(),
            branchIndex);
      }
      final var whileDef =
          routeDefinition
              .loopDoWhile()
              .method(ScenarioOrchestrationHandlers.handleContextPredicate(branch.predicate()));
      branch.statements().forEach(statement -> buildRouteForStatement(whileDef, statement));
      whileDef.end();
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
          "Unhandled statement type '%s' used in conditional-branch of orchestration for integration-scenario %s",
          statement.getClass().getName(), getIntegrationScenarioId());
    }
  }
}
