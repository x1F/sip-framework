package one.x1f.sip.foundation.core.actuator.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.function.Function;
import org.apache.camel.Endpoint;
import org.apache.commons.collections4.MultiValuedMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;

class EndpointHealthRegistryTest {

  private static final String ENDPOINT_URI = "test";
  private static final String ENDPOINT_ID = "testId";

  private EndpointHealthRegistry subject;
  private Function<Endpoint, Health> healthFunction;
  private Endpoint endpoint;

  @BeforeEach
  void setUp() {
    subject = new EndpointHealthRegistry();
    healthFunction = ep -> Health.up().build();
    endpoint = mock(Endpoint.class);
  }

  @Test
  void When_registerNewHealthFunctionByEndpointUri_Expect_healthIndicatorMatchersIsNotEmpty() {
    // arrange
    when(endpoint.getEndpointUri()).thenReturn(ENDPOINT_URI);

    // act
    assertThat(subject.getHealthIndicatorMatchers()).isEmpty();
    subject.register(ENDPOINT_URI, healthFunction);

    // assert
    assertThat(subject.getHealthIndicatorMatchers()).isNotEmpty();
  }

  @Test
  void When_gettingHealthIndicatorForEndpoint_Expect_healthIndicatorIsPresent() {
    // arrange
    when(endpoint.getEndpointUri()).thenReturn(ENDPOINT_URI);
    subject.register(ENDPOINT_URI, healthFunction);

    // act
    Optional<EndpointHealthIndicator> healthIndicator = subject.healthIndicator(endpoint);

    // assert
    assertThat(healthIndicator).isNotNull().isPresent();
  }

  @Test
  void
      When_registerNewHealthFunctionByProcessorId_Expect_matchersByProcessorIdContainsEndpointIdAsKey() {
    // act
    subject.registerById(ENDPOINT_ID, healthFunction);
    MultiValuedMap<String, Function<Endpoint, Health>> matchers =
        subject.getMatchersByProcessorId();

    // assert
    assertThat(matchers.containsKey(ENDPOINT_ID)).isTrue();
    assertThat(matchers.get(ENDPOINT_ID)).contains(healthFunction);
  }

  @Test
  void When_healthIndicatorIsNotRegistered_Expect_notPresent() {
    // arrange
    when(endpoint.getEndpointUri()).thenReturn(ENDPOINT_URI);

    // act
    Optional<EndpointHealthIndicator> healthIndicator = subject.healthIndicator(endpoint);

    // assert
    assertThat(healthIndicator).isNotNull().isNotPresent();
  }
}
