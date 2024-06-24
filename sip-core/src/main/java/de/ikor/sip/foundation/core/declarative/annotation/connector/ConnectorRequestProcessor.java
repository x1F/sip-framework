package de.ikor.sip.foundation.core.declarative.annotation.connector;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that is used to mark a processor that should be attached to the request-flow of a
 * Connector.
 *
 * <p>The annotation can either be placed on a separate class implementing {@link
 * de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor}, in which case the
 * connector it should be attached to <em>must</em> be provided via {@link #value()} or {@link
 * #connectorId()}.
 *
 * <p>Alternatively, the annotation can be placed on a method inside a connector class. Using this
 * approach, no additional attributes need to be specified, and the following usage patterns are
 * possible:
 *
 * <ul>
 *   <li>The method can take no arguments and return a {@link
 *       de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor} instance
 *   <li>The method can take any number of arguments, and the framework does a best effort to assign
 *       the parameters accordingly:
 *       <ul>
 *         <li>Parameters of type {@link org.apache.camel.Exchange} or {@link
 *             org.apache.camel.Message} will receive their current respective instance
 *         <li>Parameters annotated with @{@link HeaderParameter} will receive the content of the
 *             header with the specified name, if it exists
 *         <li>For any other parameter types, the framework will attempt a mandatory conversion (if
 *             necessary) of the current body to the declared type
 *         <li>Parameters can be annotated @{@link javax.annotation.Nullable} to declare that <code>
 *             null</code> is permitted
 *         <li>If the method declares a non-void return type, the returned object of the method call
 *             will be set as the new body on the current message
 *       </ul>
 * </ul>
 *
 * <p>For ordering of multiple processors, {@link ConnectorProcessorOrder} can be used for absolute
 * ordering, while {@link RunAfterConnectorProcessor} or {@link RunBeforeConnectorProcessor} can be
 * used for relative ordering.
 *
 * @see de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor
 * @see ConnectorResponseProcessor
 * @see RunAfterConnectorProcessor
 * @see RunBeforeConnectorProcessor
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ConnectorRequestProcessor {

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
