package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;

/**
 * Functional interface used to determine number of iterations of for loop in process orchestration
 *
 * @param <M> the response model type of the orchestrated scenario
 */
@FunctionalInterface
public interface ScenarioStepIterations<M> {

  /**
   * @param context - Orchestration context from which the number of iterations will be determined
   * @return - number of iterations
   */
  int determineIterations(ScenarioOrchestrationContext<M> context);
}
