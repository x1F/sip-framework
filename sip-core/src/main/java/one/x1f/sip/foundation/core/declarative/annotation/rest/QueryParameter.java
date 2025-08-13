package one.x1f.sip.foundation.core.declarative.annotation.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.HeaderParameter;

/**
 * Indicates a REST query-parameter to be assigned to the annotated parameter of a mapping-function
 * annotated with @{@link ParameterMapping}.
 *
 * <p>This annotation is an alias for @{@link HeaderParameter}.
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
