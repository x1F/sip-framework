package de.ikor.sip.foundation.core.declarative.orchestration.process.routebuilding;

import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationHandlers;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.process.dsl.*;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.ProcessorDefinition;

/**
 * Class for generating Camel routes for while loop process consumer calls from a DSL
 *
 * <p><em>For internal use only</em>
 */
@Slf4j
@SuppressWarnings("rawtypes")
final class RouteGeneratorForWhileLoopProcessConsumer extends RouteGeneratorProcessBase {

  private final CallWhileLoopStatement<?> loopStatement;

  private final Set<IntegrationScenarioDefinition> overallUnhandledConsumers;

  RouteGeneratorForWhileLoopProcessConsumer(
      final CompositeProcessOrchestrationInfo orchestrationInfo,
      final CallWhileLoopStatement<?> loopStatement,
      final Set<IntegrationScenarioDefinition> overallUnhandledConsumers) {
    super(orchestrationInfo);
    this.loopStatement = loopStatement;
    this.overallUnhandledConsumers = overallUnhandledConsumers;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    List<CallWhileLoopStatement.ProcessBranchStatements> loopProcess =
        RouteGeneratorInternalHelper.getLoopProcess(loopStatement);
    if (loopProcess.isEmpty()) {
      throw SIPFrameworkInitializationException.init(
          "Empty loop statement attached in orchestration for composite process '%s'",
          getCompositeProcessId());
    }
    loopProcess.forEach(
        branch -> {
          var loopDef =
              routeDefinition.loopDoWhile(
                  exchange ->
                      CompositeProcessOrchestrationHandlers.handleConditional(
                          exchange,
                          RouteGeneratorInternalHelper.getStepResultCloner(loopStatement),
                          Optional.ofNullable(branch.predicate())));

          branch.statements().forEach(statement -> buildRouteForStatement(loopDef, statement));
          loopDef.end();
        });
  }

  private <T extends ProcessorDefinition<T>> void buildRouteForStatement(
      final T routeDefinition, final CallableWithinProcessDefinition statement) {
    if (statement instanceof CallProcessConsumer callDef) {
      new RouteGeneratorForCallProcessConsumer(
              getOrchestrationInfo(), callDef, overallUnhandledConsumers)
          .generateRoute(routeDefinition);
    } else {
      throw SIPFrameworkInitializationException.init(
          "Unhandled statement used in loop-branch of orchestration for composite process '%s'",
          getCompositeProcessId());
    }
  }
}
