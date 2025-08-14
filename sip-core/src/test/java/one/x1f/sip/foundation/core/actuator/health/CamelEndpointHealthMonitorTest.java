package one.x1f.sip.foundation.core.actuator.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Optional;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.ServiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CamelEndpointHealthMonitorTest {

  private static final String ENDPOINT_NAME = "test";

  private CamelContext camelContext;
  private CamelEndpointHealthMonitor subject;
  private EndpointHealthRegistry endpointHealthRegistry;

  @BeforeEach
  void setup() {
    endpointHealthRegistry = mock(EndpointHealthRegistry.class);
    camelContext = mock(CamelContext.class, RETURNS_DEEP_STUBS);
    subject = new CamelEndpointHealthMonitor(camelContext, endpointHealthRegistry);
  }

  @Test
  void
      Given_EndpointHealthIndicatorExists_When_setupEndpointHealthIndicators_Then_HealthIndicatorsIsNotEmpty() {
    // arrange
    EndpointHealthIndicator endpointHealthIndicator = mock(EndpointHealthIndicator.class);
    when(camelContext.getEndpoints()).thenReturn(Arrays.asList(mock(Endpoint.class)));
    when(camelContext.getStatus()).thenReturn(ServiceStatus.Started);
    when(endpointHealthIndicator.name()).thenReturn(ENDPOINT_NAME);
    when(endpointHealthRegistry.healthIndicator(any()))
        .thenReturn(Optional.of(endpointHealthIndicator));

    // act
    assertThat(subject.getHealthIndicators()).isEmpty();
    subject.setupEndpointHealthIndicators();

    // assert
    assertThat(subject.getHealthIndicators()).isNotEmpty();
  }

  @Test
  void
      Given_NoEndpointHealthIndicatorsExist_When_setupEndpointHealthIndicators_Then_HealthIndicatorsIsEmpty() {
    // arrange
    Endpoint endpoint = mock(Endpoint.class);
    when(camelContext.getEndpoints()).thenReturn(Arrays.asList(endpoint));
    when(camelContext.getStatus()).thenReturn(ServiceStatus.Started);

    // act
    subject.setupEndpointHealthIndicators();

    // assert
    verify(endpointHealthRegistry, times(1)).healthIndicator(endpoint);
    assertThat(subject.getHealthIndicators()).isEmpty();
  }
}
