package one.x1f.sip.foundation.core.declarative.orchestration;

/**
 * Common interface for elements that allow to be orchestrated by a matching {@link Orchestrator}.
 *
 * @see Orchestrator
 * @param <T> Type holding the information needed for the orchestration
 */
public interface Orchestratable<T extends OrchestrationInfo> {

  /**
   * Returns the orchestrator that can orchestrate this element.
   *
   * @return orchestrator capable of orchestrating this element
   */
  Orchestrator<T> getOrchestrator();
}
