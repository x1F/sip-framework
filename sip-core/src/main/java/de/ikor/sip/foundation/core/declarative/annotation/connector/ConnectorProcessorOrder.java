package de.ikor.sip.foundation.core.declarative.annotation.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used for absolute ordering of {@link
 * de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor}s.
 *
 * <p>Can be used on types or methods annotated via {@link ConnectorRequestProcessor} or {@link
 * ConnectorResponseProcessor}.
 *
 * @see ConnectorRequestProcessor
 * @see ConnectorResponseProcessor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectorProcessorOrder {

  /**
   * @return Position number on which this processor should be run
   */
  int value();
}
