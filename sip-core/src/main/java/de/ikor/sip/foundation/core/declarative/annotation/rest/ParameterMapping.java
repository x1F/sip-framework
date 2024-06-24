package de.ikor.sip.foundation.core.declarative.annotation.rest;

import de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorRequestProcessor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method within a {@link
 * de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase} that is used to map
 * path- and query-parameters to the request-model.
 *
 * <p>To use, create a public method using this annotation within your implementation of the
 * connector class. You can use any number of parameters for that method, and the framework will
 * automatically assign the corresponding data using a best-effort transformation the declared type.
 * The following rules apply:
 *
 * <ul>
 *   <li>Parameters annotated with @{@link PathParameter} will be filled with the respectively named
 *       path parameter
 *   <li>Parameters annotated with @{@link QueryParameter} will be filled with the respectively
 *       named query parameter
 *   <li>Parameters of type {@link org.apache.camel.Exchange} will receive the exchange
 *   <li>Parameters of type {@link org.apache.camel.Message} will receive the message
 *   <li>Parameters of any other type will receive the message body in the declared type
 * </ul>
 *
 * If the provided data from the request can not be converted to the declared parameter type, the
 * REST request will return a server error.
 *
 * <p>Example:
 *
 * <pre>{@code
 * @InboundConnector
 * public class RestApi extends RestInboundConnectorBase {
 *
 *     @Override
 *     protected void configureRest(final RestDefinition definition) {
 *       definition
 *           .post("/api/{first}/{second}")
 *           .type(ApiModel.class);
 *     }
 *
 *     @ParameterMapping
 *     public void mapParameters(ApiModel model, @PathParameter("first") Integer pathFirst, @PathParameter("second") String pathSecond) {
 *          model.setFirst(pathFirst);
 *          model.setSecond(pathSecond);
 *     }
 * }
 *
 * }</pre>
 *
 * @see PathParameter
 * @see QueryParameter
 * @see de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ConnectorRequestProcessor
public @interface ParameterMapping {}
