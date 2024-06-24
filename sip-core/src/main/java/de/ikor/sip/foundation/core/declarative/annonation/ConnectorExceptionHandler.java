package de.ikor.sip.foundation.core.declarative.annonation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method level annotation used to declare methods in Connectors which handle specific exceptions
 * provided in annotation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectorExceptionHandler {
  /**
   * Array of Exception which should be handled by the annotated method
   *
   * @return array of Exceptions
   */
  Class<? extends Throwable>[] value();
}
