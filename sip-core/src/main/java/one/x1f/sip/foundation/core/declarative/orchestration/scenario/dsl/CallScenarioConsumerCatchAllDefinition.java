package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/** DSL class for calling all remaining scenario consumers */
public final class CallScenarioConsumerCatchAllDefinition<R, M>
    extends CallScenarioConsumerBaseDefinition<CallScenarioConsumerCatchAllDefinition<R, M>, R, M> {

  CallScenarioConsumerCatchAllDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }
}
