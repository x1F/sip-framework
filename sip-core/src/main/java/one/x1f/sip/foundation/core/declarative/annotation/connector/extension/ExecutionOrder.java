package one.x1f.sip.foundation.core.declarative.annotation.connector.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorExtension;

/** Annotation used for absolute ordering of {@link ConnectorExtension}s. */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecutionOrder {

  /**
   * @return Position number on which this processor should be run
   */
  int value() default -1;

  /**
   * @return Whether this should be the first extension for this connector. Can only be declared
   *     once per Connector.
   */
  boolean first() default false;

  /**
   * @return Whether if this should be the last extension for this connector. Can only be declared
   *     once per Connector.
   */
  boolean last() default false;
}
