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
final class RouteGeneratorForConditionalCallScenarioConsumerDefinition<M>
    extends RouteGeneratorBase {
  private final ConditionalCallScenarioConsumerDefinition<?, M> conditionalDefinition;

  private final Set<IntegrationScenarioConsumerDefinition> overallUnhandledConsumers;

  RouteGeneratorForConditionalCallScenarioConsumerDefinition(
      final ScenarioOrchestrationInfo orchestrationInfo,
      final ConditionalCallScenarioConsumerDefinition<?, M> conditionalDefinition,
      final Set<IntegrationScenarioConsumerDefinition> overallUnhandledConsumers) {
    super(orchestrationInfo);
    this.conditionalDefinition = conditionalDefinition;
    this.overallUnhandledConsumers = overallUnhandledConsumers;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    if (conditionalDefinition.getConditionalStatements().isEmpty()) {
      throw SIPFrameworkInitializationException.init(
          "Empty conditional statement attached in orchestration for integration-scenario %s",
          getIntegrationScenarioId());
    }

    final var choiceDef = routeDefinition.choice();
    for (final var branch : conditionalDefinition.getConditionalStatements()) {
      if (branch.statements().isEmpty()) {
        var branchIndex = conditionalDefinition.getConditionalStatements().indexOf(branch) + 1;
        log.warn(
            "Orchestration for integration-scenario {} contains a conditional-statement that does not specify any actions in branch #{}",
            getIntegrationScenarioId(),
            branchIndex);
      }
      choiceDef
          .when()
          .method(ScenarioOrchestrationHandlers.handleContextPredicate(branch.predicate()));
      branch.statements().forEach(statement -> buildRouteForStatement(choiceDef, statement));
      choiceDef.endChoice();
    }

    if (!conditionalDefinition.getUnconditionalStatements().isEmpty()) {
      choiceDef.otherwise();
      conditionalDefinition
          .getUnconditionalStatements()
          .forEach(statement -> buildRouteForStatement(choiceDef, statement));
      choiceDef.endChoice();
    }

    choiceDef.end();
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
