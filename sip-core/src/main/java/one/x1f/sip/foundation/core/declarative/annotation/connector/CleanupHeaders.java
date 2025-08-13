package one.x1f.sip.foundation.core.declarative.annotation.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.connector.InboundConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connector.OutboundConnectorDefinition;

/**
 * Annotation to be used in conjunction with connectors when it is necessary to clean up headers
 * before further processing.
 *
 * <ul>
 *   <li>When used on an {@link InboundConnectorDefinition inbound connector}, the headers will be
 *       removed before the message is passed to the integration scenario, and recreated once the
 *       response is returning into the connector.
 *   <li>When used on an {@link OutboundConnectorDefinition outbound connector}, the headers will be
 *       removed before the message is passed to the external endpoint, and recreated once the
 *       response is returning into the connector.
 * </ul>
 *
 * <p>Headers that should remain can be defined via {@link #keep()}
 *
 * @see OutboundConnectorDefinition
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CleanupHeaders {

  /**
   * @return Regex patterns for headers that should be kept
   */
  String[] keep() default {};
}
