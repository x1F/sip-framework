package one.x1f.sip.foundation.core.declarative.orchestration;

/**
 * Generic Interface for orchestrators that can orchestrate an element with help of information
 * provided through {@link OrchestrationInfo}.
 */
public interface Orchestrator<T extends OrchestrationInfo> {

  /**
   * Specified whether the given <code>info</code> is sufficient for the orchestrator to fulfill the
   * orchestration
   *
   * @param info Orchestration info
   * @return Orchestration possible based on given info
   */
  boolean canOrchestrate(T info);

  /**
   * Initiates the orchestration
   *
   * @param info Info provided to the orchestrator
   */
  void doOrchestrate(T info);
}
