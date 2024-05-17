package de.ikor.sip.foundation.core.declarative.annotation.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method within a {@link
 * de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase} that is used to map
 * path- and query-parameters to the request-model.
 *
 * @see PathParameter
 * @see QueryParameter
 * @see de.ikor.sip.foundation.core.declarative.connector.RestInboundConnectorBase
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterMapping {}
