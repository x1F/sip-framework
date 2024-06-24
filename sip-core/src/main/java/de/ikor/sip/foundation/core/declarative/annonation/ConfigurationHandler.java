package de.ikor.sip.foundation.core.declarative.annonation;

import de.ikor.sip.foundation.core.declarative.configuration.ConfigurationDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Annotation used on classes extending {@link
 * de.ikor.sip.foundation.core.declarative.connector.ConnectorBase} or {@link
 * de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase} It provides information
 * which {@link ConfigurationDefinition} classes should be applied to the respective Scenarios or
 * Connectors
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface ConfigurationHandler {
  /**
   * Classes extending {@link ConfigurationDefinition} to the respective Scenario or Connector
   *
   * @return array of configuration classes
   */
  Class<? extends ConfigurationDefinition>[] value() default {};
}
