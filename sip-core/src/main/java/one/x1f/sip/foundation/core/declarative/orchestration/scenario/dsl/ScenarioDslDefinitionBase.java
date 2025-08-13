package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import lombok.AccessLevel;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.DslDefinitionBase;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/**
 * Base element for DSL classes used to orchestrate in integration scenario
 *
 * @param <M> type of the integration scenario's response model
 */
public abstract class ScenarioDslDefinitionBase<S extends ScenarioDslDefinitionBase<S, R, M>, R, M>
    extends DslDefinitionBase<S, R> {

  @Getter(AccessLevel.PACKAGE)
  private final IntegrationScenarioDefinition integrationScenario;

  ScenarioDslDefinitionBase(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition);
    this.integrationScenario = integrationScenario;
  }
}
