package de.ikor.sip.foundation.core.declarative.annotation.rest;

import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.HeaderParameter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a REST query-parameter to be assigned to the annotated parameter of a mapping-function
 * annotated with @{@link ParameterMapping}.
 *
 * <p>This annotation is an alias for @{@link
 * HeaderParameter}.
 *
 * @see HeaderParameter
 * @see ParameterMapping
 * @see PathParameter
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParameter {

  /**
   * @return Name of the query-parameter
   */
  String value();
}
