package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.Optional;

/**
 * Interface to aggregate the response of an individual scenario consumer call into an overall
 * aggregated response
 *
 * @param <M> Response type of the integration call
 */
@FunctionalInterface
public interface ScenarioStepResponseAggregator<M> {

  /**
   * Aggregates the response of an individual scenario consumer call into an overall aggregated
   * response.
   *
   * @param latestResponse Response of the consumer call
   * @param previousOverallResponse Previous aggregated response, if it exists
   * @return The new aggregated response
   */
  M aggregateResponse(M latestResponse, Optional<M> previousOverallResponse);
}
