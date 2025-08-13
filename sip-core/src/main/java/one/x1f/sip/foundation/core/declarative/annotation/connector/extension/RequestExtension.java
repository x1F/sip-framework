package one.x1f.sip.foundation.core.declarative.annotation.connector.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorExtension;

/**
 * Annotation that is used to mark an extension that should be attached to the request-flow of a
 * Connector.
 *
 * <p>The annotation can either be placed on a separate class implementing {@link
 * ConnectorExtension}, in which case the connector it should be attached to <em>must</em> be
 * provided via {@link #value()} or {@link #connectorId()}.
 *
 * <p>Alternatively, the annotation can be placed on a method inside a connector class. Using this
 * approach, no additional attributes need to be specified, and the following usage patterns are
 * possible:
 *
 * <ul>
 *   <li>The method can take no arguments and return a {@link ConnectorExtension} instance
 *   <li>The method returns <code>void</code> and has a single parameter of type {@link
 *       org.apache.camel.model.RouteDefinition}.
 * </ul>
 *
 * <p>For ordering of multiple processors, {@link ExecutionOrder} can be used for absolute ordering,
 * while {@link ExecuteAfter} or {@link ExecuteBefore} can be used for relative ordering.
 *
 * @see ConnectorExtension
 * @see ResponseExtension
 * @see ExecuteAfter
 * @see ExecuteBefore
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestExtension {

  /**
   * @return Optional link to the connector this extension should belong to (only necessary if
   *     placed on a class)
   */
  Class<? extends ConnectorDefinition> value() default ConnectorDefinition.None.class;

  /**
   * @return Optional connector-id of the connector this extension should belong to (only necessary
   *     if placed on a class)
   */
  String connectorId() default "";
}
