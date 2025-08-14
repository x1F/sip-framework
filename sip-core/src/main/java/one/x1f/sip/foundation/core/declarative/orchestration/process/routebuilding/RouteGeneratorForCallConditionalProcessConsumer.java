package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationHandlers;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallNestedCondition;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallProcessConsumer;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallableWithinProcessDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.RouteGeneratorInternalHelper;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.model.ProcessorDefinition;

/**
 * Class for generating Camel routes for conditional process consumer calls from a DSL
 *
 * <p><em>For internal use only</em>
 */
@Slf4j
@SuppressWarnings("rawtypes")
final class RouteGeneratorForCallConditionalProcessConsumer extends RouteGeneratorProcessBase {

  private final CallNestedCondition<?> conditionalDefinition;

  private final Set<IntegrationScenarioDefinition> overallUnhandledConsumers;

  RouteGeneratorForCallConditionalProcessConsumer(
      final CompositeProcessOrchestrationInfo orchestrationInfo,
      final CallNestedCondition conditionalDefinition,
      final Set<IntegrationScenarioDefinition> overallUnhandledConsumers) {
    super(orchestrationInfo);
    this.conditionalDefinition = conditionalDefinition;
    this.overallUnhandledConsumers = overallUnhandledConsumers;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    List<CallNestedCondition.ProcessBranchStatements> conditionalStatements =
        RouteGeneratorInternalHelper.getConditionalStatements(conditionalDefinition);
    if (conditionalStatements.isEmpty()) {
      throw SIPFrameworkInitializationException.init(
          "Empty conditional statement attached in orchestration for composite process '%s'",
          getCompositeProcessId());
    }

    final var choiceDef = routeDefinition.choice();
    for (final var branch : conditionalStatements) {
      if (branch.statements().isEmpty()) {
        var branchIndex = conditionalStatements.indexOf(branch) + 1;
        log.warn(
            "Orchestration for composite process {} contains a conditional-statement that does not specify any actions in branch #{}",
            getCompositeProcess().getId(),
            branchIndex);
      }
      choiceDef.when(
          exchange ->
              CompositeProcessOrchestrationHandlers.handleConditional(
                  exchange,
                  RouteGeneratorInternalHelper.getStepResultCloner(conditionalDefinition),
                  Optional.ofNullable(branch.predicate())));
      branch.statements().forEach(statement -> buildRouteForStatement(choiceDef, statement));
      choiceDef.endChoice();
    }
    List<CallableWithinProcessDefinition> unconditionalStatements =
        RouteGeneratorInternalHelper.getUnconditionalStatements(conditionalDefinition);

    if (!unconditionalStatements.isEmpty()) {
      choiceDef.otherwise();
      unconditionalStatements.forEach(statement -> buildRouteForStatement(choiceDef, statement));
      choiceDef.endChoice();
    }

    choiceDef.end();
  }

  private <T extends ProcessorDefinition<T>> void buildRouteForStatement(
      final T routeDefinition, final CallableWithinProcessDefinition statement) {
    if (statement instanceof CallProcessConsumer callDef) {
      new RouteGeneratorForCallProcessConsumer(
              getOrchestrationInfo(), callDef, overallUnhandledConsumers)
          .generateRoute(routeDefinition);
    } else {
      throw SIPFrameworkInitializationException.init(
          "Unhandled statement type '%s' used in conditional-branch of orchestration for composite process '%s'",
          getCompositeProcessId());
    }
  }
}
