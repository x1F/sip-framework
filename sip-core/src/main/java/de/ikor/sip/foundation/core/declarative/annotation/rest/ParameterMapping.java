package de.ikor.sip.foundation.core.declarative.annotation.rest;

import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.HeaderParameter;
import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.RequestProcessor;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorExtension;
import de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method within a {@link
 * de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase} that is used to map
 * path- and query-parameters to the request-model. *
 *
 * <p>The parameter mapping functionality is based on {@link ConnectorExtension} extensions as
 * described in the @{@link RequestProcessor}. Hence, this annotation is merely an alias of latter
 * annotation. Likewise, @{@link QueryParameter} and {@link PathParameter} annotations are aliases
 * for @{@link HeaderParameter}, as these parameters are placed in the message as headers by Camel.
 *
 * <p>Note that parameter-mapping using this approach only works if you don't overload {@link
 * RestInboundConnectorBase#defineTransformationOrchestrator()} in your connector.
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
 * @see RequestProcessor
 * @see PathParameter
 * @see QueryParameter
 * @see de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RequestProcessor
public @interface ParameterMapping {}
