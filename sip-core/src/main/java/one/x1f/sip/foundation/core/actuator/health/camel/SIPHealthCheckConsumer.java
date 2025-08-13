package one.x1f.sip.foundation.core.actuator.health.camel;

import java.util.Map;
import java.util.Objects;
import org.apache.camel.ServiceStatus;
import org.apache.camel.health.HealthCheckResultBuilder;
import org.apache.camel.impl.health.ConsumerHealthCheck;

public class SIPHealthCheckConsumer extends ConsumerHealthCheck {

  SIPHealthCheckConsumer(ConsumerHealthCheck routeHealthCheck) {
    super(
        routeHealthCheck
            .getCamelContext()
            .getRoute(routeHealthCheck.getId().replaceFirst("consumer:", "")),
        routeHealthCheck.getId());
  }

  @Override
  protected void doCallCheck(HealthCheckResultBuilder builder, Map<String, Object> options) {
    String routeStatus = (String) builder.detail("route.status");
    if (Objects.equals(routeStatus, ServiceStatus.Suspended.name())) {
      builder.down();
    }
    super.doCallCheck(builder, options);
  }
}
