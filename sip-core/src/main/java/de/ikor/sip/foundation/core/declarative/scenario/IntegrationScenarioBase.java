package de.ikor.sip.foundation.core.declarative.scenario;

import de.ikor.sip.foundation.core.declarative.annotation.ConfigurationHandler;
import de.ikor.sip.foundation.core.declarative.annotation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.AutoMagicScenarioOrchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.StandardScenarioOrchestrators;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import org.springframework.util.ClassUtils;

/**
 * Base class for an integration scenario definition.
 *
 * <p>Adapter developers should extend this class and annotate it with @{@link IntegrationScenario}.
 *
 * @see IntegrationScenario
 */
public abstract class IntegrationScenarioBase implements IntegrationScenarioDefinition {

  private final IntegrationScenario scenarioAnnotation =
      DeclarativeReflectionUtils.getAnnotationOrThrow(IntegrationScenario.class, this);

  private final Optional<ConfigurationHandler> declarativeConfigurationAnnotation =
      DeclarativeReflectionUtils.getAnnotationIfPresent(ConfigurationHandler.class, this);

  /**
   * Returns the orchestrator for this scenario.
   *
   * <p>The default implementation tries to apply default-orchestrators from {@link
   * StandardScenarioOrchestrators} automatically.
   *
   * <p>If, in more complex cases, a custom orchestration is required, this method should be
   * overloaded and the scenario specified in orchestration-DSL initialized via {@link
   * de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator#forOrchestrationDslWithResponse(Class,
   * Consumer)} or {@link
   * de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator#forOrchestrationDslWithoutResponse(Consumer)}.
   *
   * @see de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator
   * @return Orchestrator for this scenario
   */
  @Override
  public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
    return new AutoMagicScenarioOrchestrator(
        StandardScenarioOrchestrators.ANY_TO_ONE,
        StandardScenarioOrchestrators.ANY_TO_ANY_WITHOUT_RESPONSE);
  }

  @Override
  public final String getId() {
    return scenarioAnnotation.scenarioId();
  }

  @Override
  public final Class<? extends Object> getRequestModelClass() {
    return scenarioAnnotation.requestModel();
  }

  @Override
  public final Optional<Class<? extends Object>> getResponseModelClass() {
    var responseModel = scenarioAnnotation.responseModel();
    return responseModel.equals(Void.class) ? Optional.empty() : Optional.of(responseModel);
  }

  @Override
  public String getPathToDocumentationResource() {
    return scenarioAnnotation.pathToDocumentationResource();
  }

  @Override
  public final String[] getConfigurationIds() {
    return declarativeConfigurationAnnotation
        .map(
            configurationHandler ->
                Arrays.stream(configurationHandler.value())
                    .map(ClassUtils::getShortName)
                    .toArray(String[]::new))
        .orElseGet(() -> new String[0]);
  }
}
