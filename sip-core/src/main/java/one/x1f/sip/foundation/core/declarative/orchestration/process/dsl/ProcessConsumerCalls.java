package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/**
 * DSL interface for calling a consumer by its {@link IntegrationScenarioDefinition} class
 *
 * @param <S> DSL handle for caller
 * @param <R> DSL handle for the return DSL Verb/type.
 */
interface ProcessConsumerCalls<S extends ProcessConsumerCalls<S, R>, R> {

  @SuppressWarnings(
      "java:S1452") // suppressing warning, wildcard on return type used only internally
  CallProcessConsumer<? extends CallProcessConsumer, S> callConsumer(
      Class<? extends IntegrationScenarioDefinition> consumerClass);
}
