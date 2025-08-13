package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;

/**
 * Interface to extract the request object for a scenario consumer call
 *
 * @param <M> Response type of the integration call
 */
@FunctionalInterface
public interface ScenarioStepRequestExtractor<M> {

  /**
   * Returns the request object to be used with the consumer call
   *
   * @param context The current orchestration context
   * @return Request object
   */
  Object extractStepRequest(final ScenarioOrchestrationContext<M> context);
}
