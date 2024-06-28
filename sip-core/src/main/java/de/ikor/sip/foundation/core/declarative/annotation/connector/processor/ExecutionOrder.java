package de.ikor.sip.foundation.core.declarative.annotation.connector.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for absolute ordering of {@link
 * de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor}s.
 *
 * <p>Can be used on types or methods annotated via {@link RequestProcessor} or {@link
 * ResponseProcessor}.
 *
 * @see RequestProcessor
 * @see ResponseProcessor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionOrder {

  /**
   * @return Position number on which this processor should be run
   */
  int value() default -1;

  /**
   * @return Whether this should be the first processor for this connector. Can only be declared
   *     once per Connector.
   */
  boolean first() default false;

  /**
   * @return Whether if this should be the last processor for this connector. Can only be declared
   *     once per Connector.
   */
  boolean last() default false;
}
