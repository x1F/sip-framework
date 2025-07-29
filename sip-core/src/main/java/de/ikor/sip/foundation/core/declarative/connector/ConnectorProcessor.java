package de.ikor.sip.foundation.core.declarative.connector;

import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;

public interface ConnectorProcessor extends ConnectorExtension, Processor {

  @Override
  default void accept(RouteDefinition routeDefinition) {
    routeDefinition.process(this);
  }

  default String getProcessorName() {
    return getClass().getSimpleName();
  }

  @Override
  default String getExtensionName() {
    return getProcessorName();
  }
}
