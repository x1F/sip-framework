package de.ikor.sip.foundation.core.declarative.annotation.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
  String value();
}
