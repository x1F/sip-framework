package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.function.Consumer;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

public final class ProcessCallScenarioConsumerDefinition<R, M>
    extends ScenarioDslDefinitionBase<ProcessCallScenarioConsumerDefinition<R, M>, R, M>
    implements CallableWithinProviderDefinition {
  @Getter Consumer<ScenarioOrchestrationContext<M>> consumer;

  ProcessCallScenarioConsumerDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }

  R process(Consumer<ScenarioOrchestrationContext<M>> consumer) {
    this.consumer = consumer;
    return getDslReturnDefinition();
  }
}
