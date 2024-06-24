package de.ikor.sip.foundation.core.declarative.connector;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Interface that marks {@link Processor}s that can be placed within the integration flow of a
 * connector.
 *
 * @see de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorRequestProcessor
 * @see de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorResponseProcessor
 */
public interface ConnectorProcessor extends Processor {
  default String getProcessorName() {
    return getClass().getSimpleName();
  }

  /**
   * Empty {@link ConnectorProcessor} implementation that is used for default assignments in
   * annotations
   */
  final class None implements ConnectorProcessor {
    @Override
    public void process(final Exchange exchange) throws Exception {
      throw new UnsupportedOperationException();
    }
  }
}
