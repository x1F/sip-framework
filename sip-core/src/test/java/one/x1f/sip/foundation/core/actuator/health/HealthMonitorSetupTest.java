package one.x1f.sip.foundation.core.actuator.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.processor.SendProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HealthMonitorSetupTest {

  private static final String ENDPOINT_URI = "endpoint";
  private static final String PROCESSOR_ID = "processor";

  private EndpointHealthRegistry endpointHealthRegistry;
  private HealthMonitorSetup subject;

  @Mock private CamelContext camelContext;

  @Mock private CamelEndpointHealthMonitor camelEndpointHealthMonitor;

  @BeforeEach
  void setUp() {
    endpointHealthRegistry = new EndpointHealthRegistry();
    subject =
        new HealthMonitorSetup(camelContext, endpointHealthRegistry, camelEndpointHealthMonitor);

    when(camelContext.getProcessor(PROCESSOR_ID)).thenReturn(mock(SendProcessor.class));
    when(((SendProcessor) camelContext.getProcessor(PROCESSOR_ID)).getDestination())
        .thenReturn(mock(Endpoint.class));
    when(((SendProcessor) camelContext.getProcessor(PROCESSOR_ID))
            .getDestination()
            .getEndpointUri())
        .thenReturn(ENDPOINT_URI);
  }

  @Test
  void When_setupCamelEndpointHealthMonitor_Expect_MatchersAndIndicatorMatcherNotEmpty() {
    // arrange
    endpointHealthRegistry.registerById(PROCESSOR_ID, null);

    // act
    subject.setupCamelEndpointHealthMonitor();

    // assert
    assertThat(endpointHealthRegistry.getMatchersByProcessorId().size()).isEqualTo(1);
    assertThat(endpointHealthRegistry.getHealthIndicatorMatchers()).hasSize(1);
  }

  @Test
  void
      Given_IdAlreadyRegistered_When_setupCamelEndpointHealthMonitorAnd_Then_ThrowDuplicateUriPatternError() {
    // arrange
    endpointHealthRegistry.registerById(PROCESSOR_ID, null);
    endpointHealthRegistry.registerById(PROCESSOR_ID, null);

    // assert
    assertThatExceptionOfType(DuplicateUriPatternError.class)
        .isThrownBy(() -> subject.setupCamelEndpointHealthMonitor());
  }
}
