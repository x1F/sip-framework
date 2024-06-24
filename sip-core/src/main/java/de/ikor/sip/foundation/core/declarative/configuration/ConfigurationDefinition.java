package de.ikor.sip.foundation.core.declarative.configuration;

import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.RouteConfigurationDefinition;

/** Functional interface providing a hook to {@link RouteConfigurationDefinition} */
@FunctionalInterface
public interface ConfigurationDefinition {
  /**
   * Define route configuration handler (onException, onCompletion, intercept). There should be only
   * one per implementation.
   *
   * @param definition {@link RouteConfigurationDefinition}
   */
  @SuppressWarnings("rawtypes")
  OutputDefinition define(RouteConfigurationDefinition definition);
}
