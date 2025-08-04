package de.ikor.sip.foundation.core.declarative.annotation;

import de.ikor.sip.foundation.core.declarative.process.CompositeProcessBase;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Annotation to be used with a class extending {@link CompositeProcessBase}.
 *
 * <p>This annotation allows to easily provide information for (some) of the data required by the *
 * {@link CompositeProcessDefinition} interface.
 *
 * @see CompositeProcessBase
 * @see CompositeProcessDefinition
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface CompositeProcess {

  /**
   * @return Identifier of the composite process. Must be unique within the whole adapter.
   * @see CompositeProcessDefinition#getId()
   */
  String processId();

  /**
   * @return Class of the provider for this composite process
   * @see CompositeProcessDefinition#getConsumerDefinitions()
   */
  Class<? extends IntegrationScenarioDefinition>[] consumers();

  /**
   * @return Class of the provider for this composite process
   * @see CompositeProcessDefinition#getProviderDefinition()
   */
  Class<? extends IntegrationScenarioDefinition> provider();

  /**
   * Optional path to the resource (typically a markdown file) that describes this composite
   * process. If not given, an attempt is made to retrieve documentation from {@code
   * document/structure/processes/<composite-process-id>.md} automatically.
   *
   * @return Optional path to documentation resource file
   */
  String pathToDocumentationResource() default "";
}
