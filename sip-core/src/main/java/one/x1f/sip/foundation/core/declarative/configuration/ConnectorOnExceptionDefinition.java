package one.x1f.sip.foundation.core.declarative.configuration;

import one.x1f.sip.foundation.core.declarative.annotation.ConnectorExceptionHandler;
import org.apache.camel.model.OnExceptionDefinition;

/**
 * Functional interface providing a hook to {@link OnExceptionDefinition}.
 *
 * <p>Used with {@link ConnectorExceptionHandler} annotation
 *
 * @see ConnectorExceptionHandler
 */
@FunctionalInterface
public interface ConnectorOnExceptionDefinition {

  /**
   * Define onException handler. The exception types that should be handled are provided via {@link
   * ConnectorExceptionHandler}
   *
   * @param definition {@link OnExceptionDefinition}
   */
  void define(OnExceptionDefinition definition);
}
