package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.Collections;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/** DSL class specifying all an inbound connector specified by its connector ID */
public final class ForScenarioProvidersByConnectorIdDefinition<R, M>
    extends ForScenarioProvidersBaseDefinition<
        ForScenarioProvidersByConnectorIdDefinition<R, M>, R, M> {

  @Getter(AccessLevel.PACKAGE)
  private final Set<String> connectorIds;

  ForScenarioProvidersByConnectorIdDefinition(
      final R dslReturnDefinition,
      final IntegrationScenarioDefinition integrationScenario,
      final Set<String> connectorIds) {
    super(dslReturnDefinition, integrationScenario);
    this.connectorIds = Collections.unmodifiableSet(connectorIds);
  }
}
