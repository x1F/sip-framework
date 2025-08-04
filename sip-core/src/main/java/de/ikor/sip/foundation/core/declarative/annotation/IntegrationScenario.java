package de.ikor.sip.foundation.core.declarative.annotation;

import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Annotation to be used with a class extending {@link
 * de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase}.
 *
 * <p>This annotation allows to easily provide information for (some) of the data required by the
 * {@link de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition} interface.
 *
 * @see de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase
 * @see de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface IntegrationScenario {

  /**
   * @return Identifier of the integration scenario. Must be unique within the whole adapter.
   * @see IntegrationScenarioDefinition#getId()
   */
  String scenarioId();

  /**
   * @return Common request model base class for this scenario
   * @see IntegrationScenarioDefinition#getRequestModelClass()
   */
  Class<?> requestModel();

  /**
   * @return Optional common response model base class for this scenario
   * @see IntegrationScenarioDefinition#getResponseModelClass()
   */
  Class<?> responseModel() default Void.class;

  /**
   * Optional path to the resource (typically a markdown file) that describes this integration
   * scenario. If not given, an attempt is made to retrieve documentation from {@code
   * document/structure/integration-scenarios/<integration-scenario-id>.md} automatically.
   *
   * @return Optional path to documentation resource file
   */
  String pathToDocumentationResource() default "";
}
