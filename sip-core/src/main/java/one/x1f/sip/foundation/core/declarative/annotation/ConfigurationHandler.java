package one.x1f.sip.foundation.core.declarative.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.configuration.ConfigurationDefinition;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorBase;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;
import org.springframework.stereotype.Component;

/**
 * Annotation used on classes extending {@link ConnectorBase} or {@link IntegrationScenarioBase}
 *
 * <p>It marks which {@link ConfigurationDefinition} classes should be applied to the respective
 * Scenarios or Connectors
 *
 * @see ConfigurationDefinition
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
