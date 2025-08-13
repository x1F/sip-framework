package one.x1f.sip.foundation.core.declarative.annotation.connector.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorProcessor;

/**
 * Annotation that is used to mark a processor that should be attached to the request-flow of a
 * Connector.
 *
 * <p>The annotation can either be placed on a separate class implementing {@link
 * ConnectorProcessor}, in which case the connector it should be attached to <em>must</em> be
 * provided via {@link #value()} or {@link #connectorId()}.
 *
 * <p>Alternatively, the annotation can be placed on a method inside a connector class. Using this
 * approach, no additional attributes need to be specified, and the following usage patterns are
 * possible:
 *
 * <ul>
 *   <li>The method can take no arguments and return a {@link ConnectorProcessor} instance
 *   <li>The method can take any number of arguments, and the framework does a best effort to assign
 *       the parameters accordingly:
 *       <ul>
 *         <li>Parameters of type {@link org.apache.camel.Exchange} or {@link
 *             org.apache.camel.Message} will receive their current respective instance
 *         <li>Parameters annotated with @{@link HeaderParameter} will receive the content of the
 *             header with the specified name, if it exists.
 *         <li>For any other parameter types, the framework will attempt a mandatory conversion (if
 *             necessary) of the current body to the declared type
 *         <li>Parameters can be annotated @{{@link jakarta.annotation.Nullable}} to declare that
 *             <code>
 *             null</code> is permitted
 *         <li>If the method declares a non-void return type, the returned object of the method call
 *             will be set as the new body on the current message
 *       </ul>
 * </ul>
 *
 * <p>For ordering of multiple processors, {@link ExecutionOrder} can be used for absolute ordering,
 * while {@link ExecuteAfter} or {@link ExecuteBefore} can be used for relative ordering.
 *
 * @see ConnectorProcessor
 * @see ResponseProcessor
 * @see ExecuteAfter
 * @see ExecuteBefore
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestProcessor {

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
