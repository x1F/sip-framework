package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;

/**
 * Class for generating Camel routes for scenario orchestration via DSL
 *
 * <p><em>For internal use only</em>
 */
@SuppressWarnings("rawtypes")
public final class RouteGeneratorForScenarioOrchestrationDefinition<M> extends RouteGeneratorBase
    implements Runnable {

  private final ScenarioOrchestrationDefinition<M> scenarioOrchestrationDefinition;

  public RouteGeneratorForScenarioOrchestrationDefinition(
      final ScenarioOrchestrationInfo orchestrationInfo,
      final ScenarioOrchestrationDefinition<M> scenarioOrchestrationDefinition) {
    super(orchestrationInfo);
    this.scenarioOrchestrationDefinition = scenarioOrchestrationDefinition;
  }

  @Override
  public void run() {
    final var scenarioProvidersOverall = getOrchestrationInfo().getProviderEndpoints().keySet();
    final var unhandledProvidersOverall = new HashSet<>(scenarioProvidersOverall);
    final List<RouteGeneratorForScenarioProvidersDefinition> providerBuilders = new ArrayList<>();

    for (ForScenarioProvidersBaseDefinition providerDefinition :
        scenarioOrchestrationDefinition.getScenarioProviderDefinitions()) {

      final var builder =
          new RouteGeneratorForScenarioProvidersDefinition<M>(
              getOrchestrationInfo(),
              providerDefinition,
              Collections.unmodifiableSet(unhandledProvidersOverall));

      if (builder.getHandledProviders().isEmpty()) {
        continue;
      }
      unhandledProvidersOverall.removeAll(builder.getHandledProviders());
      providerBuilders.add(builder);
    }

    providerBuilders.forEach(builder -> builder.generateRoutes(getRoutesDefinition()));
  }
}
