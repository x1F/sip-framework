package one.x1f.sip.foundation.core.actuator.health.scheduler;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import one.x1f.sip.foundation.core.actuator.health.CamelEndpointHealthMonitor;
import org.springframework.boot.health.contributor.Status;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to export overall status from HealthStatus with micrometer to provide metrics on
 * this
 */
@Configuration
@HealthCheckEnabledCondition
public class HealthCheckMetricsConfiguration {

  /**
   * Creates new instance of HealthCheckMetricsConfiguration
   *
   * @param registry {@link MeterRegistry}
   * @param monitor {@link CamelEndpointHealthMonitor}
   * @param healthGaugeConfiguration {@link HealthGaugeConfiguration}
   */
  public HealthCheckMetricsConfiguration(
      MeterRegistry registry,
      CamelEndpointHealthMonitor monitor,
      HealthGaugeConfiguration healthGaugeConfiguration) {
    Gauge.builder(healthGaugeConfiguration.getName(), monitor, this::getStatusCode)
        .strongReference(true)
        .register(registry);
  }

  private int getStatusCode(CamelEndpointHealthMonitor monitor) {
    return monitor.getHealthIndicators().values().stream()
            .allMatch(
                endpointHealthIndicator ->
                    endpointHealthIndicator.health().getStatus().equals(Status.UP))
        ? 0
        : 1;
  }
}
