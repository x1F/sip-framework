package one.x1f.sip.foundation.core.actuator.health;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Endpoint;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/**
 * {@link EndpointHealthIndicator} wraps health-checking function into a Spring Boot's {@link
 * HealthIndicator} interface.
 */
@RequiredArgsConstructor
public class EndpointHealthIndicator implements HealthIndicator {
  private final Endpoint endpoint;
  private final Function<Endpoint, Health> healthFunction;
  private Health health;

  /**
   * Returns the name of the endpoint. It will be used and visible in the health information report
   * as the identifier of this endpoint.
   *
   * @return String
   */
  public String name() {
    return endpoint.getEndpointUri();
  }

  /**
   * Returns health of the endpoint.
   *
   * @return Health
   */
  @Override
  public Health health() {
    return health;
  }

  /** Calculates health of the endpoint. */
  public void executeHealthCheck() {
    this.health = healthFunction.apply(this.endpoint);
  }
}
