package de.ikor.sip.foundation.core.declarative.orchestration.process.routebuilding;

import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;

@FunctionalInterface
public interface ScenarioStepIterations<M> {

  /**
   * @param context - Orchestration context for which the condition will be checked
   * @return - result of the check
   */
  int determineIterations(ScenarioOrchestrationContext<M> context);
}
