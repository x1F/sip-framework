package one.x1f.sip.foundation.core.actuator.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.function.Function;
import org.apache.camel.Endpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

class EndpointHealthIndicatorTest {

  private static final String ENDPOINT_URI = "uri";

  private EndpointHealthIndicator subject;
  private Endpoint endpoint;
  private Health health;

  @BeforeEach
  void setup() {
    endpoint = mock(Endpoint.class);
    health = Health.up().build();
    Function<Endpoint, Health> healthFunction = ep -> health;
    subject = new EndpointHealthIndicator(endpoint, healthFunction);
  }

  @Test
  void WHEN_fetchingName_EXPECT_endpointNameReturned() {
    // arrange
    when(endpoint.getEndpointUri()).thenReturn(ENDPOINT_URI);

    // act
    String nameResult = subject.name();

    // assert
    assertThat(nameResult).isEqualTo(ENDPOINT_URI);
  }

  @Test
  void WHEN_health_WITH_NoHealthCalculation_THEN_NoHealth() {
    // act
    Health healthResult = subject.health();

    // assert
    assertThat(healthResult).isNull();
  }

  @Test
  void WHEN_health_WITH_HealthCalculation_THEN_ReturnHealth() {
    // act
    subject.executeHealthCheck();
    Health healthResult = subject.health();

    // assert
    assertThat(healthResult).isEqualTo(health);
  }

  @Test
  void WHEN_executeHealthCheck_EXPECT_HealthIsCalculated() {
    // act
    subject.executeHealthCheck();
    Health healthResult = subject.getHealth(false);

    // assert
    assertThat(healthResult).isEqualTo(health);
  }
}
