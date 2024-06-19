package de.ikor.sip.foundation.core.declarative.configuration;

import org.apache.camel.model.OnExceptionDefinition;

/** Functional interface providing a hook to {@link OnExceptionDefinition} */
@FunctionalInterface
public interface DeclarativeOnExceptionDefinition {

  /**
   * Define onException configuration
   *
   * @param definition {@link OnExceptionDefinition}
   */
  void define(OnExceptionDefinition definition);
}
