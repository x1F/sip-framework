package one.x1f.sip.foundation.core.actuator.health.camel;

import java.util.stream.Stream;
import org.apache.camel.DeferredContextBinding;
import org.apache.camel.health.HealthCheck;
import org.apache.camel.impl.health.RouteHealthCheck;
import org.apache.camel.impl.health.RoutesHealthCheckRepository;

@DeferredContextBinding
public class SIPHealthCheckRoutesRepository extends RoutesHealthCheckRepository {

  public SIPHealthCheckRoutesRepository() {
    super();
  }

  @Override
  public Stream<HealthCheck> stream() {

    return super.stream()
        .filter(RouteHealthCheck.class::isInstance)
        .map(RouteHealthCheck.class::cast)
        .map(SIPHealthCheckRoute::new);
  }
}
