package de.ikor.sip.foundation.core.declarative.annotation.connector;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used to mark a processor that should be attached to the response-flow of a
 * Connector.
 *
 * <p>Usage of this annotation is identical to {@link ConnectorRequestProcessor}, so please check
 * there for more detailed instructions.
 *
 * @see ConnectorRequestProcessor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectorResponseProcessor {

  /**
   * @return Optional link to the connector this processor should belong to (only necessary if
   *     placed on a class)
   */
  Class<? extends ConnectorDefinition> value() default ConnectorDefinition.None.class;

  /**
   * @return Optional connector-id of the connector this processor should belong to (only necessary
   *     if placed on a class)
   */
  String connectorId() default "";
}
