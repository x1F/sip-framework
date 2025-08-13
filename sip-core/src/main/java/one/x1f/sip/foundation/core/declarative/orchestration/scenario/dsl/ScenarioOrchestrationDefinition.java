package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.connector.InboundConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.EndOfDsl;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioProviderDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;

/**
 * DSL class for specifying orchestration of scenario providers
 *
 * @param <M> The response model type of the integration scenario
 */
public class ScenarioOrchestrationDefinition<M>
    extends ScenarioDslDefinitionBase<ScenarioOrchestrationDefinition<M>, EndOfDsl, M> {

  @Getter(AccessLevel.PACKAGE)
  private final List<ForScenarioProvidersBaseDefinition<?, ?, M>> scenarioProviderDefinitions =
      new ArrayList<>();

  @Getter(AccessLevel.PACKAGE)
  private boolean catchAllAdded = false;

  /**
   * Constructor
   *
   * <p><em>For internal use only</em>
   *
   * @param integrationScenario Integration scenario
   */
  public ScenarioOrchestrationDefinition(final IntegrationScenarioDefinition integrationScenario) {
    super(null, integrationScenario);
  }

  /**
   * Specifies inbound connectors that should be orchestrated further by their class.
   *
   * <p>No order of execution is guaranteed if more than one class is provided - call this function
   * multiple times if this is necessary.
   *
   * @param connectorClass The class(es) of the inbound connector
   * @return DSL handle for specifying consumer calls
   */
  @SafeVarargs
  public final ForScenarioProvidersByClassDefinition<ScenarioOrchestrationDefinition<M>, M>
      forInboundConnectors(final Class<? extends InboundConnectorDefinition<?>>... connectorClass) {
    return forScenarioProviders(connectorClass);
  }

  /**
   * Specifies scenario providers that should be orchestrated further by their connector class.
   *
   * <p>No order of execution is guaranteed if more than one class is provided - call this function
   * multiple times if this is necessary.
   *
   * @param providerClass The class(es) of the inbound connector
   * @return DSL handle for specifying consumer calls
   */
  @SafeVarargs
  public final ForScenarioProvidersByClassDefinition<ScenarioOrchestrationDefinition<M>, M>
      forScenarioProviders(
          final Class<? extends IntegrationScenarioProviderDefinition>... providerClass) {
    verifyNoCatchAllOrThrow();
    final ForScenarioProvidersByClassDefinition<ScenarioOrchestrationDefinition<M>, M> def =
        new ForScenarioProvidersByClassDefinition<>(
            self(), getIntegrationScenario(), Set.of(providerClass));
    scenarioProviderDefinitions.add(def);
    return def;
  }

  private void verifyNoCatchAllOrThrow() {
    if (catchAllAdded) {
      throw new SIPFrameworkException(
          "Catch-all definition already added via method forAnyUnspecifiedProvider(). No further definitions allowed.");
    }
  }

  /**
   * Specifies inbound connectors that should be orchestrated further by their connector ID.
   *
   * <p>No order of execution is guaranteed if more than one ID is provided - call this function
   * multiple times if this is necessary.
   *
   * @param inboundConnectorId The ID(s) of the inbound connector
   * @return DSL handle for specifying consumer calls
   */
  public ForScenarioProvidersByConnectorIdDefinition<ScenarioOrchestrationDefinition<M>, M>
      forInboundConnectors(final String... inboundConnectorId) {
    verifyNoCatchAllOrThrow();
    final ForScenarioProvidersByConnectorIdDefinition<ScenarioOrchestrationDefinition<M>, M> def =
        new ForScenarioProvidersByConnectorIdDefinition<>(
            self(), getIntegrationScenario(), Set.of(inboundConnectorId));
    scenarioProviderDefinitions.add(def);
    return def;
  }

  /**
   * Allows to specify orchestration for all scenario providers (which includes inbound connectors)
   * that are not specifically declared before.
   *
   * <p>This is a terminal operation for the provider definitions, so it needs to be the * last call
   * in the list and no additional provider definitions can be specified afterwards.
   *
   * @return DSL handle for specifying consumer calls
   */
  public ForScenarioProvidersCatchAllDefinition<EndOfDsl, M> forAnyUnspecifiedScenarioProvider() {
    verifyNoCatchAllOrThrow();
    catchAllAdded = true;
    final ForScenarioProvidersCatchAllDefinition<EndOfDsl, M> def =
        new ForScenarioProvidersCatchAllDefinition<>(EndOfDsl.INSTANCE, getIntegrationScenario());
    scenarioProviderDefinitions.add(def);
    return def;
  }
}
