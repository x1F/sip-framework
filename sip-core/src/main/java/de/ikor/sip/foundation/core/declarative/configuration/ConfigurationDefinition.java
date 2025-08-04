package de.ikor.sip.foundation.core.declarative.configuration;

import de.ikor.sip.foundation.core.declarative.annotation.ConfigurationHandler;
import org.apache.camel.model.OutputDefinition;
import org.apache.camel.model.RouteConfigurationDefinition;

/**
 * Functional interface providing a hook to {@link RouteConfigurationDefinition}.
 *
 * <p>Only one handler should should be provided per implementation
 *
 * <p>Example
 *
 * <pre>{@code
 * @Configuration
 * public class DefaultConfigurationDefinition implements ConfigurationDefinition {
 *
 *   @Override
 *   public OutputDefinition<?> define(RouteConfigurationDefinition definition) {
 *     return definition
 *         .onException(SIPAdapterException.class)
 *         .process(exchange -> exchange.getMessage().setBody("message"))
 *         .handled(true);
 *   }
 * }
 * }</pre>
 *
 * To mark a scenario or connector to use the handler defined here {@link ConfigurationHandler}
 * annotation should be used
 *
 * @see ConfigurationHandler
 */
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
