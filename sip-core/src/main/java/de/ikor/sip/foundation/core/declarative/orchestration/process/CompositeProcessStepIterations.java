package de.ikor.sip.foundation.core.declarative.orchestration.process;

/** Interface to match the condition from the orchestration context */
@FunctionalInterface
public interface CompositeProcessStepIterations {

  /**
   * @param context - Orchestration context for which the condition will be checked
   * @return - result of the check
   */
  int determineIterations(CompositeProcessOrchestrationContext context);
}
