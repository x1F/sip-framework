package one.x1f.sip.foundation.core.actuator.health.scheduler;

import java.util.Map;
import lombok.AllArgsConstructor;
import one.x1f.sip.foundation.core.actuator.health.CamelEndpointHealthMonitor;
import one.x1f.sip.foundation.core.actuator.health.EndpointHealthIndicator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled execution for connection checks.
 *
 * <p>Switched on by default. Default fixed delay (interval): every 15 minutes, or 900000ms Default
 * initial delay: 5 seconds, or 5000ms
 *
 * <p>sip.core.metrics.external-endpoint-health-check.enabled:true
 * sip.core.metrics.external-endpoint-health-check.scheduler.fixed-delay:900000
 * sip.core.metrics.external-endpoint-health-check.scheduler.initial-delay:5000
 */
@Service
@AllArgsConstructor
@EnableScheduling
@HealthCheckEnabledCondition
public class ScheduledHealthCheck {

  private final CamelEndpointHealthMonitor monitor;

  /** Scheduled health check */
  @Scheduled(
      // Default scheduler values are set in sip-core-default-config.yaml
      fixedDelayString = "${sip.core.metrics.external-endpoint-health-check.scheduler.fixed-delay}",
      initialDelayString =
          "${sip.core.metrics.external-endpoint-health-check.scheduler.initial-delay}")
  public void scheduledExecution() {
    Map<String, EndpointHealthIndicator> healthIndicators = monitor.getHealthIndicators();

    healthIndicators.values().forEach(EndpointHealthIndicator::executeHealthCheck);
  }
}
