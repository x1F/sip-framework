package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import lombok.AccessLevel;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/** DSL class for calling a scenario consumer specified by it's class */
public sealed class CallScenarioConsumerByClassDefinition<R, M>
    extends CallScenarioConsumerBaseDefinition<CallScenarioConsumerByClassDefinition<R, M>, R, M>
    permits CallScenarioConsumerBaseNoResponseDefinition {

  @Getter(AccessLevel.PACKAGE)
  private final Class<? extends IntegrationScenarioConsumerDefinition> consumerClass;

  CallScenarioConsumerByClassDefinition(
      final R dslReturnDefinition,
      final IntegrationScenarioDefinition integrationScenario,
      final Class<? extends IntegrationScenarioConsumerDefinition> consumerClass) {
    super(dslReturnDefinition, integrationScenario);
    this.consumerClass = consumerClass;
  }
}
