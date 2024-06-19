package de.ikor.sip.foundation.core.declarative.configuration;

import org.apache.camel.model.RouteConfigurationDefinition;

/** Functional interface providing a hook to {@link RouteConfigurationDefinition} */
@FunctionalInterface
public interface DeclarativeConfigurationDefinition {
  /**
   * Define route configuration
   *
   * @param definition {@link RouteConfigurationDefinition}
   */
  void define(RouteConfigurationDefinition definition);
}
