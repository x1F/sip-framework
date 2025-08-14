package one.x1f.sip.foundation.core.declarative.orchestration.scenario;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;

/**
 * Enum of default orchestrator implementations that can be used with an integration scenario.
 *
 * @see ScenarioOrchestrator
 */
@RequiredArgsConstructor
public enum StandardScenarioOrchestrators
    implements Supplier<Orchestrator<ScenarioOrchestrationInfo>> {

  /**
   * Orchestrator that supports scenarios with just one consumer. If that consumer provides a
   * response, it is passed through without any modification.
   */
  ANY_TO_ONE(
      () ->
          ScenarioOrchestrator.forOrchestrationDslWithResponse(
                  Object.class,
                  dsl ->
                      dsl.forAnyUnspecifiedScenarioProvider().callAnyUnspecifiedScenarioConsumer())
              .setCanOrchestrate(info -> info.getConsumerEndpoints().size() == 1)),
  /**
   * Orchestrator that supports multiple consumers for a scenario that doesn't expect any response
   */
  ANY_TO_ANY_WITHOUT_RESPONSE(
      () ->
          ScenarioOrchestrator.forOrchestrationDslWithoutResponse(
                  dsl ->
                      dsl.forAnyUnspecifiedScenarioProvider()
                          .callAnyUnspecifiedScenarioConsumer()
                          .andNoResponseHandling())
              .setCanOrchestrate(info -> !info.getIntegrationScenario().hasResponseFlow()));

  private final Supplier<Orchestrator<ScenarioOrchestrationInfo>> delegate;

  @Override
  public Orchestrator<ScenarioOrchestrationInfo> get() {
    return delegate.get();
  }
}
