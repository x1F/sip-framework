package de.ikor.sip.foundation.core.declarative.annotation;

import de.ikor.sip.foundation.core.declarative.configuration.ConnectorOnExceptionDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method level annotation used on methods in Connectors which handle specific exceptions provided
 * as parameters in annotation.
 *
 * <p>Must be used on public methods which have {@link ConnectorOnExceptionDefinition} as return
 * type.
 *
 * <p>Exception handler defined on this level will take priority over other.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @ConnectorExceptionHandler(RuntimeException.class)
 * public ConnectorOnExceptionDefinition define() {
 *   return onException ->
 *        onException
 *           .setBody(simple("message"))
 *           .handled(true);
 * }
 * }</pre>
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
