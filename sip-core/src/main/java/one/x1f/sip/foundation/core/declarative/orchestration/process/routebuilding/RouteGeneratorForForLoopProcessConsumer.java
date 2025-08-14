package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import static one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationHandlers.handleIterations;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessStepIterations;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallForLoopStatement;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallProcessConsumer;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallableWithinProcessDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.RouteGeneratorInternalHelper;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.support.ExpressionAdapter;

/**
 * Class for generating Camel routes for 'for' loop process consumer calls from a DSL
 *
 * <p><em>For internal use only</em>
 */
@Slf4j
@SuppressWarnings("rawtypes")
final class RouteGeneratorForForLoopProcessConsumer extends RouteGeneratorProcessBase {

  private final CallForLoopStatement<?> loopStatement;

  private final Set<IntegrationScenarioDefinition> overallUnhandledConsumers;

  RouteGeneratorForForLoopProcessConsumer(
      final CompositeProcessOrchestrationInfo orchestrationInfo,
      final CallForLoopStatement<?> loopStatement,
      final Set<IntegrationScenarioDefinition> overallUnhandledConsumers) {
    super(orchestrationInfo);
    this.loopStatement = loopStatement;
    this.overallUnhandledConsumers = overallUnhandledConsumers;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    List<CallForLoopStatement.ProcessBranchStatements> loopProcess =
        RouteGeneratorInternalHelper.getForLoopProcess(loopStatement);
    if (loopProcess.isEmpty()) {
      throw SIPFrameworkInitializationException.init(
          "Empty loop statement attached in orchestration for composite process '%s'",
          getCompositeProcessId());
    }
    loopProcess.forEach(
        branch -> {
          var loopDef =
              routeDefinition.loop(new IterationsExpression(Optional.of(branch.iterations())));
          branch.statements().forEach(statement -> buildRouteForStatement(loopDef, statement));
          loopDef.end();
        });
  }

  @RequiredArgsConstructor
  static class IterationsExpression extends ExpressionAdapter {
    private final Optional<CompositeProcessStepIterations> iterations;

    @Override
    public Object evaluate(Exchange exchange) {
      return handleIterations(exchange, iterations);
    }
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
