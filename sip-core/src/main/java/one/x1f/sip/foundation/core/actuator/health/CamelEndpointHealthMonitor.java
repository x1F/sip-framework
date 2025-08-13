package one.x1f.sip.foundation.core.actuator.health;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.NamedContributor;

/**
 * {@link CamelEndpointHealthMonitor} is a central point in evaluating Health information of the
 * {@link Endpoint}s in the current {@link CamelContext}.
 *
 * <p>It uses {@link EndpointHealthRegistry} to create {@link HealthIndicator}s for context
 * endpoints.
 *
 * <p>As endpoints are not available at the moment of construction, state design pattern has been
 * used in order to collect information about them after the first invocation of the {@link
 * CamelEndpointHealthMonitor#iterator()} method.
 */
@RequiredArgsConstructor
public class CamelEndpointHealthMonitor implements CompositeHealthContributor {
  private final CamelContext camelContext;
  private final EndpointHealthRegistry registry;
  private Map<String, EndpointHealthIndicator> healthIndicators = new HashMap<>();

  @Override
  public HealthContributor getContributor(String name) {
    return getHealthIndicators().get(name);
  }

  @Override
  public Iterator<NamedContributor<HealthContributor>> iterator() {
    return healthIndicators();
  }

  public synchronized Map<String, EndpointHealthIndicator> getHealthIndicators() {
    return healthIndicators;
  }

  Iterator<NamedContributor<HealthContributor>> setupEndpointHealthIndicators() {
    Collection<Endpoint> endpoints = camelContext.getEndpoints();
    if (camelContext.getStatus().isStarted()) {
      this.healthIndicators =
          endpoints.stream()
              .flatMap(
                  endpoint ->
                      registry.healthIndicator(endpoint).map(Stream::of).orElseGet(Stream::empty))
              .collect(
                  Collectors.toConcurrentMap(EndpointHealthIndicator::name, endpoint -> endpoint));
    }
    return healthIndicators();
  }

  Iterator<NamedContributor<HealthContributor>> healthIndicators() {
    return getHealthIndicators().values().stream()
        .map(indicator -> NamedContributor.of(indicator.name(), (HealthContributor) indicator))
        .collect(Collectors.toSet())
        .iterator();
  }
}
