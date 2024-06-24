package de.ikor.sip.foundation.core.declarative.annotation.rest;

import de.ikor.sip.foundation.core.declarative.annotation.connector.HeaderParameter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

/**
 * Indicates a REST path-parameter to be assigned to the annotated parameter of a mapping-function
 * annotated with @{@link ParameterMapping}
 *
 * @see ParameterMapping
 * @see QueryParameter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParameter {

  /**
   * @return Name of the path parameter
   */
  @AliasFor(annotation = HeaderParameter.class, attribute = "value")
  String value();
}
