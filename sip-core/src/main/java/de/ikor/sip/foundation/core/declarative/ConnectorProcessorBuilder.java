package de.ikor.sip.foundation.core.declarative;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ConnectorProcessorBuilder {

  private final ConnectorDefinition connector;

  ConnectorProcessorBuilder(ConnectorDefinition connector) {
    this.connector = connector;
  }
}
