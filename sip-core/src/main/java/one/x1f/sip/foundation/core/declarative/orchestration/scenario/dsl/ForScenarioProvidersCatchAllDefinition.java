package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/** DSL class specifying all remaining scenario providers */
public final class ForScenarioProvidersCatchAllDefinition<R, M>
    extends ForScenarioProvidersBaseDefinition<ForScenarioProvidersCatchAllDefinition<R, M>, R, M> {

  ForScenarioProvidersCatchAllDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }
}
