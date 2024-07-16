package de.ikor.sip.foundation.core.declarative.annotation.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used in conjunction with outbound connectors when it is necessary to clean up
 * headers before sending the message to the external system.
 *
 * <p>Headers will only be removed for the call and re-added once the outbound call has completed.
 *
 * <p>Headers that should remain can be defined via {@link #keep()}
 *
 * @see de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CleanupHeaders {

  /**
   * @return Regex patterns for headers that should be kept for the outbound call
   */
  String[] keep() default {};
}
