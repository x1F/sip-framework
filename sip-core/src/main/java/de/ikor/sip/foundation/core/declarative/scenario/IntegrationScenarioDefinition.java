package de.ikor.sip.foundation.core.declarative.scenario;

import de.ikor.sip.foundation.core.declarative.DeclarativeElement;
import de.ikor.sip.foundation.core.declarative.annotation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestratable;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import java.util.Optional;

/**
 * Interface used for specifying integration scenarios used within a SIP adapter.
 *
 * <p>Integration scenarios allow for decoupling of the connectors, as they define common domain
 * models that must be used and mapped to/from by all connectors that provide or consume from the
 * scenario.
 *
 * <p><em>Adapter developers should not implement this interface directly, but use {@link
 * IntegrationScenarioBase} instead.</em>
 *
 * @see IntegrationScenarioBase
 * @see IntegrationScenario
 */
public interface IntegrationScenarioDefinition
    extends Orchestratable<ScenarioOrchestrationInfo>, DeclarativeElement {

  /**
   * Returns the ID of the integration scenario. Must be unique within the scope of the adapter.
   *
   * <p>The scenario identifier should typically relate to the functional/business process the
   * integration is supporting.
   *
   * @return Integration scenario identifier
   */
  String getId();

  /**
   * Base class of the common request model used by the scenario. The model can be shared across
   * multiple scenarios.
   *
   * @return Request model base class
   */
  Class<?> getRequestModelClass();

  /**
   * Base class of the common response model used by the scenario. The model can be shared across
   * multiple scenarios.
   *
   * @return Response model base class, or {@link Optional#empty()} if the scenario does not have a
   *     response flow
   */
  Optional<Class<?>> getResponseModelClass();

  /**
   * Returns whether the scenario has a response flow. The default implementation returns <code>true
   * </code> if a response-model is set.
   *
   * @return True if the scenario has a response flow, false otherwise
   */
  default boolean hasResponseFlow() {
    return getResponseModelClass().isPresent();
  }

  /**
   * Returns an array of configuration ids which should be applied to the given scenario routes
   *
   * @return configuration ids
   */
  String[] getConfigurationIds();
}
