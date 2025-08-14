package one.x1f.sip.foundation.core.declarative;

import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;

@Slf4j
public final class ConnectorProcessorBuilder {

  private final ConnectorDefinition connector;

  ConnectorProcessorBuilder(ConnectorDefinition connector) {
    this.connector = connector;
  }
}
