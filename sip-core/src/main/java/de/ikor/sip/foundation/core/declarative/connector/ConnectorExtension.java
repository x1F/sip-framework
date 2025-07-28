package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.RequestProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.ResponseProcessor;
import java.util.function.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.model.RouteDefinition;

/**
 * Interface that marks {@link Processor}s that can be placed within the integration flow of a
 * connector.
 *
 * @see RequestProcessor
 * @see ResponseProcessor
 */
public interface ConnectorExtension extends Consumer<RouteDefinition> {

  default String getExtensionName() {
    return getClass().getSimpleName();
  }

  /*  */
  /**
   * Empty {@link ConnectorExtension} implementation that is used for default assignments in
   * annotations
   */
  /*
  final class None implements ConnectorExtension {}*/
}
