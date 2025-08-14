package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.Collections;
import java.util.Set;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioProviderDefinition;

/** DSL class specifying all a scenario provider specified by its class */
public final class ForScenarioProvidersByClassDefinition<R, M>
    extends ForScenarioProvidersBaseDefinition<ForScenarioProvidersByClassDefinition<R, M>, R, M> {

  private final Set<Class<? extends IntegrationScenarioProviderDefinition>> providerClasses;

  ForScenarioProvidersByClassDefinition(
      final R dslReturnDefinition,
      final IntegrationScenarioDefinition integrationScenario,
      final Set<Class<? extends IntegrationScenarioProviderDefinition>> providerClasses) {
    super(dslReturnDefinition, integrationScenario);
    this.providerClasses = Collections.unmodifiableSet(providerClasses);
  }

  Set<Class<? extends IntegrationScenarioProviderDefinition>> getProviderClasses() {
    return providerClasses;
  }
}
