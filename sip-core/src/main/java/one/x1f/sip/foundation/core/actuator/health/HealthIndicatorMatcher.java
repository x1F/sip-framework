package one.x1f.sip.foundation.core.actuator.health;

import java.util.function.Function;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Endpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HealthIndicatorMatcher} contains health checking function and criteria for applying it.
 */
@RequiredArgsConstructor
class HealthIndicatorMatcher {
  private static final Logger logger = LoggerFactory.getLogger(HealthIndicatorMatcher.class);

  private final Predicate<Endpoint> matcher;
  private final Function<Endpoint, EndpointHealthIndicator> factory;

  boolean matches(Endpoint endpoint) {
    return matcher.test(endpoint);
  }

  EndpointHealthIndicator indicator(Endpoint endpoint) {
    logger.trace("Creating health indicator for {}", endpoint.getEndpointUri());
    return factory.apply(endpoint);
  }

  public Predicate<Endpoint> getMatcher() {
    return matcher;
  }
}
