package one.x1f.sip.foundation.core.actuator.health;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.processor.SendProcessor;
import org.apache.commons.collections4.MapIterator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * {@link HealthMonitorSetup} is a class which executes pre setup for health check, collects and
 * stores health checking functions registered by endpoint id.
 */
@Slf4j
@Component
@AllArgsConstructor
public class HealthMonitorSetup {
  private final CamelContext camelContext;
  private final EndpointHealthRegistry registry;
  private final CamelEndpointHealthMonitor monitor;

  /**
   * Registers health checking functions for the given processor ids. Checks if there are uri
   * pattern duplicates and shuts down in that case. Sets up endpoint health indicators.
   */
  @EventListener(ApplicationReadyEvent.class)
  public void setupCamelEndpointHealthMonitor() {
    MapIterator<String, Function<Endpoint, Health>> itr =
        registry.getMatchersByProcessorId().mapIterator();
    while (itr.hasNext()) {
      registerHealthMatcherByProcessorId(itr.next(), itr.getValue());
    }
    if (checkDuplicates(registry.getHealthIndicatorMatchers())) {
      throw new DuplicateUriPatternError(
          "Error occurred while setup health check indicator matchers - "
              + "2 or more identical uri patterns used.");
    }
    monitor.setupEndpointHealthIndicators();
  }

  /**
   * Fetch concrete endpoint uri pattern by processor id and register it as health indicator
   * matcher.
   *
   * @param processorId is processor id.
   * @param healthCheckFunction is one endpoint matcher.
   */
  private void registerHealthMatcherByProcessorId(
      String processorId, Function<Endpoint, Health> healthCheckFunction) {
    if (camelContext.getProcessor(processorId) != null) {
      String uri =
          ((SendProcessor) camelContext.getProcessor(processorId))
              .getDestination()
              .getEndpointUri();
      registry.register(uri, healthCheckFunction);
    } else {
      log.warn("sip.core.health.missingprocessorid_{}", processorId);
    }
  }

  /**
   * Check for uri pattern duplicates among the all registered health checking functions.
   *
   * @param healthIndicatorMatchers health checking matchers.
   * @return returns boolean, true when duplicates found, false when there are no duplicates.
   */
  private boolean checkDuplicates(List<HealthIndicatorMatcher> healthIndicatorMatchers) {
    Set<String> uriChecker = new HashSet<>();
    for (HealthIndicatorMatcher matcher : healthIndicatorMatchers) {
      if (!uriChecker.add(((PathMatcher) matcher.getMatcher()).getPathMatcherExpression())) {
        return true;
      }
    }
    return false;
  }
}
