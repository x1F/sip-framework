package de.ikor.sip.foundation.core.declarative.connector;

import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;

/**
 * Interface that marks extensions that can be placed within the integration flow of a connector.
 *
 * <p>Connector processors are a more specific type of {@link ConnectorExtension}s that are geared
 * to work with the payload on each integration call.
 *
 * @see de.ikor.sip.foundation.core.declarative.annotation.connector.extension.RequestProcessor
 * @see de.ikor.sip.foundation.core.declarative.annotation.connector.extension.ResponseProcessor
 */
@FunctionalInterface
public interface ConnectorProcessor extends ConnectorExtension, Processor {

  /**
   * Default implementation of {@link #accept(RouteDefinition)} that attaches this processor into
   * the route. Should not usually be modified / overriden.
   */
  @Override
  default void accept(RouteDefinition routeDefinition) {
    routeDefinition.process(this);
  }

  /**
   * @deprecated Override {@link #getExtensionName()} instead where necessary
   */
  @Deprecated(since = "4.0.0")
  default String getProcessorName() {
    return getClass().getSimpleName();
  }

  @Override
  default String getExtensionName() {
    return getProcessorName();
  }
}
