package de.ikor.sip.foundation.core.declarative.annotation.connector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to be used in conjunction with connectors when it is necessary to clean up headers
 * before further processing.
 *
 * <ul>
 *   <li>When used on an {@link
 *       de.ikor.sip.foundation.core.declarative.connector.InboundConnectorDefinition inbound
 *       connector}, the headers will be removed before the message is passed to the integration
 *       scenario, and recreated once the response is returning into the connector.
 *   <li>When used on an {@link
 *       de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition outbound
 *       connector}, the headers will be removed before the message is passed to the external
 *       endpoint, and recreated once the response is returning into the connector.
 * </ul>
 *
 * <p>Headers that should remain can be defined via {@link #keep()}
 *
 * @see de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CleanupHeaders {

  /**
   * @return Regex patterns for headers that should be kept
   */
  String[] keep() default {};
}
