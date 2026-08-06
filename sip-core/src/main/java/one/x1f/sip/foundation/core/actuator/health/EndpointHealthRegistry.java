package one.x1f.sip.foundation.core.actuator.health;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.actuator.health.http.HttpHealthIndicators;
import org.apache.camel.Endpoint;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/**
 * {@link EndpointHealthRegistry} is responsible for creating {@link HealthIndicator}s for Camel
 * Endpoints in the application.
 *
 * <p>It offers dynamic rule evaluation - which HealthIndicator to use for which Endpoint, based on
 * its URI. Besides, it offers preconfigured rules for some frequently used Endpoints, such as JMS
 * or HTTP.
 *
 * <p>However, default health checking rules can easily be overridden, if needed, by the application
 * developer.
 *
 * @see EndpointHealthRegistry#register(String, Function)
 */
@Slf4j
@Getter
public class EndpointHealthRegistry {
  private final List<HealthIndicatorMatcher> healthIndicatorMatchers = new LinkedList<>();
  private final MultiValuedMap<String, Function<Endpoint, Health>> matchersByProcessorId =
      new ArrayListValuedHashMap<>();

  /**
   * Registers health checking function for the given URI pattern.
   *
   * @param uriPattern uriPattern to be used for matching Camel endpoint with the health checking
   *     function
   * @param healthFunction health checking function for an endpoint
   * @return returns {@link EndpointHealthRegistry}, enabling chained function invocation.
   * @see PathMatcher
   * @see HttpHealthIndicators
   */
  public EndpointHealthRegistry register(
      String uriPattern, Function<Endpoint, Health> healthFunction) {
    healthIndicatorMatchers.add(
        new HealthIndicatorMatcher(
            PathMatcher.of(uriPattern),
            endpoint -> new EndpointHealthIndicator(endpoint, healthFunction)));
    return this;
  }

  /**
   * Registers health checking function for the given processor id.
   *
   * @param processorId processor id for fetching the concrete endpoint uriPattern to be used for
   *     matching Camel endpoint with the health checking function.
   * @param healthFunction health checking function for an endpoint
   */
  public void registerById(String processorId, Function<Endpoint, Health> healthFunction) {
    matchersByProcessorId.put(processorId, healthFunction);
  }

  Optional<EndpointHealthIndicator> healthIndicator(Endpoint endpoint) {
    log.trace("sip.core.health.lookforindicator_{}", endpoint.getEndpointUri());

    Optional<HealthIndicatorMatcher> factoryMatcher =
        healthIndicatorMatchers.stream()
            .sorted(
                Comparator.comparingInt(
                    matcher ->
                        LevenshteinDistance.getDefaultInstance()
                            .apply(
                                endpoint.getEndpointUri(),
                                ((PathMatcher) matcher.getMatcher()).getPathMatcherExpression())))
            .filter(matcher -> matcher.matches(endpoint))
            .findFirst();

    if (!factoryMatcher.isPresent()) {
      log.trace("sip.core.health.noindicator_{}", endpoint.getEndpointUri());
    }

    return factoryMatcher.map(m -> m.indicator(endpoint));
  }
}
