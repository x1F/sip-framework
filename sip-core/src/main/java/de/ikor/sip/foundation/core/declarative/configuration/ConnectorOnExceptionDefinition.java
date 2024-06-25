package de.ikor.sip.foundation.core.declarative.configuration;

import de.ikor.sip.foundation.core.declarative.annonation.ConnectorExceptionHandler;
import org.apache.camel.model.OnExceptionDefinition;

/**
 * Functional interface providing a hook to {@link OnExceptionDefinition}.
 *
 * <p>Used with {@link de.ikor.sip.foundation.core.declarative.annonation.ConnectorExceptionHandler}
 * annotation
 *
 * @see ConnectorExceptionHandler
 */
@FunctionalInterface
public interface ConnectorOnExceptionDefinition {

  /**
   * Define onException handler. The exception types that should be handled are provided via {@link
   * de.ikor.sip.foundation.core.declarative.annonation.ConnectorExceptionHandler}
   *
   * @param definition {@link OnExceptionDefinition}
   */
  void define(OnExceptionDefinition definition);
}
