package one.x1f.sip.foundation.core.declarative.orchestration.scenario;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl.RouteGeneratorForScenarioOrchestrationDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl.ScenarioOrchestrationDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

/**
 * Orchestrator meant to be attached to integration-scenarios.
 *
 * <p>Default orchestrators can be found in {@link StandardScenarioOrchestrators}, while custom ones
 * can be defined in orchestration DSL via {@link #forOrchestrationDslWithoutResponse(Consumer)} or
 * {@link #forOrchestrationDslWithResponse(Class, Consumer)}.
 *
 * @see IntegrationScenarioBase#getOrchestrator()
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ScenarioOrchestrator implements Orchestrator<ScenarioOrchestrationInfo> {

  private final Consumer<ScenarioOrchestrationInfo> orchestrationConsumer;
  @Setter private Predicate<ScenarioOrchestrationInfo> canOrchestrate = Objects::nonNull;

  /**
   * Creates a new orchestrator specified via orchestration-DSL for scenarios that do not
   * require/specify a response model.
   *
   * @param dslDefinition Consumer that specifies the orchestration via DSL
   * @return Orchestrator as specified in the DSL
   */
  public static ScenarioOrchestrator forOrchestrationDslWithoutResponse(
      final Consumer<ScenarioOrchestrationDefinition<Void>> dslDefinition) {
    return forOrchestrationDslWithResponse(Void.class, dslDefinition);
  }

  /**
   * Creates a new orchestrator specified via orchestration-DSL for scenarios that require/specify a
   * response model.
   *
   * @param responseType The response type as defined in the integration scenario that should be
   *     orchestrated
   * @param dslDefinition Consumer that specifies the orchestration via DSL
   * @return Orchestrator as specified in the DSL
   * @param <T> Response model type
   */
  @SuppressWarnings("java:S1172")
  public static <T> ScenarioOrchestrator forOrchestrationDslWithResponse(
      final Class<T> responseType,
      final Consumer<ScenarioOrchestrationDefinition<T>> dslDefinition) {
    return forOrchestrationConsumer(
        info -> {
          final var orchestrationDef =
              new ScenarioOrchestrationDefinition<T>(info.getIntegrationScenario());
          dslDefinition.accept(orchestrationDef);
          new RouteGeneratorForScenarioOrchestrationDefinition<>(info, orchestrationDef).run();
        });
  }

  /**
   * Creates a new orchestrator specified via a consumer for the {@link ScenarioOrchestrationInfo}.
   *
   * <p>This is very low-level, and it is strongly recommended to define the orchestration via DSL
   * instead.
   *
   * @param orchestrationConsumer Consumer for the orchestration-info provided by the framework
   * @return Orchestrator
   */
  public static ScenarioOrchestrator forOrchestrationConsumer(
      final Consumer<ScenarioOrchestrationInfo> orchestrationConsumer) {
    return new ScenarioOrchestrator(orchestrationConsumer);
  }

  @Override
  public boolean canOrchestrate(final ScenarioOrchestrationInfo info) {
    return canOrchestrate.test(info);
  }

  @Override
  public void doOrchestrate(final ScenarioOrchestrationInfo info) {
    orchestrationConsumer.accept(info);
  }
}
