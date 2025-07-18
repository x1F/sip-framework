package de.ikor.sip.foundation.core.declarative.orchestration.process.routebuilding;

import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessStepSplitExpression;
import de.ikor.sip.foundation.core.declarative.orchestration.process.dsl.*;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.SplitDefinition;
import org.apache.camel.support.ExpressionAdapter;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationHandlers.handleSplitArray;

/**
 * Class for generating Camel routes for 'split' process consumer calls from a DSL
 *
 * <p><em>For internal use only</em>
 */
@Slf4j
@SuppressWarnings("rawtypes")
final class RouteGeneratorForSplitProcessConsumer extends RouteGeneratorProcessBase {

  private final CallSplitStatement<?> splitStatement;

  private final Set<IntegrationScenarioDefinition> overallUnhandledConsumers;

  RouteGeneratorForSplitProcessConsumer(
      final CompositeProcessOrchestrationInfo orchestrationInfo,
      final CallSplitStatement<?> splitStatement,
      final Set<IntegrationScenarioDefinition> overallUnhandledConsumers) {
    super(orchestrationInfo);
    this.splitStatement = splitStatement;
    this.overallUnhandledConsumers = overallUnhandledConsumers;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    List<CallSplitStatement.ProcessBranchStatements> splitProcess =
        RouteGeneratorInternalHelper.getSplitProcess(splitStatement);
    if (splitProcess.isEmpty()) {
      throw SIPFrameworkInitializationException.init(
          "Empty split statement attached in orchestration for composite process '%s'",
          getCompositeProcessId());
    }
    splitProcess.forEach(
        branch -> {
          SplitDefinition splitDef =
                  routeDefinition.split(new SplitExpression(Optional.of(branch.expression())),
                          (oldExchange, newExchange) -> newExchange);
          if (splitStatement.isParallel()) {
            splitDef.parallelProcessing().synchronous();
          }
          branch.statements().forEach(statement -> buildRouteForStatement(splitDef, statement));
          splitDef.end();
        });
  }

  @RequiredArgsConstructor
  static class SplitExpression extends ExpressionAdapter {
    private final Optional<CompositeProcessStepSplitExpression> expression;

    @Override
    public List<?> evaluate(Exchange exchange) {
      return handleSplitArray(exchange, expression);
    }
  }

  private <T extends ProcessorDefinition<T>> void buildRouteForStatement(
      final T routeDefinition, final CallableWithinProcessDefinition statement) {
    if (statement instanceof CallProcessConsumer callDef) {
      new RouteGeneratorForCallProcessConsumer(
              getOrchestrationInfo(), callDef, overallUnhandledConsumers)
          .generateRouteWithExpression(routeDefinition);
    } else {
      throw SIPFrameworkInitializationException.init(
          "Unhandled statement used in split-branch of orchestration for composite process '%s'",
          getCompositeProcessId());
    }
  }
}
