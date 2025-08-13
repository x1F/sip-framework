package one.x1f.sip.foundation.core.declarative.orchestration.process;

/** Interface to match the condition from the orchestration context */
@FunctionalInterface
public interface CompositeProcessStepConditional {

  /**
   * @param context - Orchestration context for which the condition will be checked
   * @return - result of the check
   */
  boolean determineCondition(CompositeProcessOrchestrationContext context);
}
