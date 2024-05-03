package de.ikor.sip.foundation.core.declarative.orchestration.process;

/** Interface to determine the number of loop iteration from the orchestration context */
@FunctionalInterface
public interface CompositeProcessStepIterations {

  /**
   * @param context - Orchestration context from which the loop iterations will be determined
   * @return - number of iterations
   */
  int determineIterations(CompositeProcessOrchestrationContext context);
}
