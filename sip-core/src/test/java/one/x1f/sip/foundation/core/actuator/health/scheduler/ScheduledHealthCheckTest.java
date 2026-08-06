package one.x1f.sip.foundation.core.actuator.health.scheduler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import one.x1f.sip.foundation.core.actuator.health.CamelEndpointHealthMonitor;
import one.x1f.sip.foundation.core.actuator.health.EndpointHealthIndicator;
import org.apache.camel.Endpoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;

class ScheduledHealthCheckTest {

  private static final String ENDPOINT = "endpoint";
  private static final Health HEALTH_STATUS_UP = Health.up().build();

  private CamelEndpointHealthMonitor camelEndpointHealthMonitor;
  private ScheduledHealthCheck scheduledHealthCheckSubject;

  @Test
  void WHEN_scheduledExecution_EXPECT_HealthEndpointsAreCalculated() {
    // arrange
    camelEndpointHealthMonitor = mock(CamelEndpointHealthMonitor.class);
    scheduledHealthCheckSubject = new ScheduledHealthCheck(camelEndpointHealthMonitor);

    Map<String, EndpointHealthIndicator> healthIndicators = new HashMap<>();
    Function<Endpoint, Health> healthFunction = endpoint -> HEALTH_STATUS_UP;
    EndpointHealthIndicator endpointHealthIndicator =
        new EndpointHealthIndicator(mock(Endpoint.class), healthFunction);
    healthIndicators.put(ENDPOINT, endpointHealthIndicator);

    when(camelEndpointHealthMonitor.getHealthIndicators()).thenReturn(healthIndicators);

    // act
    scheduledHealthCheckSubject.scheduledExecution();
    Health healthResult = healthIndicators.get(ENDPOINT).health(false);

    // assert
    assertThat(healthResult).isEqualTo(HEALTH_STATUS_UP);
  }
}
