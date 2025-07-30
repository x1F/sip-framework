package de.ikor.sip.foundation.core.util;

import java.util.Optional;
import lombok.experimental.UtilityClass;
import org.apache.camel.EndpointAware;
import org.apache.camel.Processor;
import org.apache.camel.processor.*;
import org.apache.commons.lang3.StringUtils;

/** Utility class to help working with specific Camel's processors. */
@UtilityClass
public class CamelProcessorsHelper {

  private static final String[] NON_OUTGOING_PROCESSOR_PREFIXES = {"seda", "direct"};

  /**
   * Method for determining if Camel processor has external endpoint.
   *
   * @param originalProcessor is processor to be checked
   * @return boolean flag (true - processor has endpoint, false - processor has no endpoint)
   */
  public static boolean isEndpointProcessor(Processor originalProcessor) {
    if (originalProcessor instanceof Enricher enricher) {
      if (enricher.getExpression() != null) {
        return !isInMemoryUri(enricher.getExpression().toString());
      }
      return true;
    }

    if (originalProcessor instanceof PollEnricher pollEnricher) {
      if (pollEnricher.getExpression() != null) {
        return !isInMemoryUri(pollEnricher.getExpression().toString());
      }
      return true;
    }

    Optional<String> endpointUri = getEndpointUri(originalProcessor);
    return endpointUri.isPresent() && !isInMemoryUri(endpointUri.get());
  }

  /**
   * Method for checking if Camel endpoint has in memory component (seda, direct).
   *
   * @param endpointUri is uri to be checked
   * @return boolean flag (true - endpointUri has in memory component)
   */
  public static boolean isInMemoryUri(String endpointUri) {
    return StringUtils.startsWithAny(endpointUri, NON_OUTGOING_PROCESSOR_PREFIXES);
  }

  /**
   * Method for getting endpointUri from specific Camel processors
   *
   * @param processor is certain type of Camel processor
   * @return Optional string with the endpoint uri
   */
  public static Optional<String> getEndpointUri(Processor processor) {
    if (processor instanceof EndpointAware endpointAware) {
      return Optional.of(endpointAware.getEndpoint().getEndpointBaseUri());
    }
    if (processor instanceof SendDynamicProcessor dynamicProcessor) {
      return Optional.of(dynamicProcessor.getUri());
    }
    if (processor instanceof WireTapProcessor wireTapProcessor) {
      return Optional.of(wireTapProcessor.getUri());
    }
    return Optional.empty();
  }
}
