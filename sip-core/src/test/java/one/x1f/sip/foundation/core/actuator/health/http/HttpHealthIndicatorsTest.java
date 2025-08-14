package one.x1f.sip.foundation.core.actuator.health.http;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.apache.camel.Endpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

class HttpHealthIndicatorsTest {

  private static final String ENDPOINT_URI = "https://www.google.com/";
  private static final String URL_KEY = "url";

  private Endpoint endpoint;

  @BeforeEach
  void setUp() {
    endpoint = mock(Endpoint.class, RETURNS_DEEP_STUBS);
  }

  @Test
  void When_alwaysHealthy_Expect_StatusUp() {
    // assert
    assertThat(HttpHealthIndicators.alwaysHealthy().getStatus()).isEqualTo(Status.UP);
  }

  @Test
  void When_alwaysUnknown_Expect_StatusUnknown() {
    // arrange
    when(endpoint.getEndpointKey()).thenReturn(ENDPOINT_URI);

    // act
    Health subject = HttpHealthIndicators.alwaysUnknown(endpoint);

    // assert
    assertThat(subject.getStatus()).isEqualTo(Status.UNKNOWN);
    assertThat(subject.getDetails()).containsEntry(URL_KEY, ENDPOINT_URI);
  }

  @Test
  void When_urlHealthIndicator_With_ValidURL_Then_StatusUp() {
    // arrange
    when(endpoint.getEndpointKey()).thenReturn(ENDPOINT_URI);

    // act
    Health subject = HttpHealthIndicators.urlHealthIndicator(endpoint);

    // assert
    assertThat(subject.getStatus()).isEqualTo(Status.UP);
    assertThat(subject.getDetails()).containsEntry(URL_KEY, ENDPOINT_URI);
  }

  @Test
  void When_urlHealthIndicator_With_InvalidURI_Then_StatusUnknown() {
    // arrange
    when(endpoint.getEndpointKey()).thenReturn(ENDPOINT_URI + "/kjk");

    // act
    Health subject = HttpHealthIndicators.urlHealthIndicator(endpoint);

    // assert
    assertThat(subject.getStatus()).isEqualTo(Status.UNKNOWN);
  }

  @Test
  void When_urlHealthIndicator_With_NonExistentURL_Then_StatusDown() {
    // arrange
    when(endpoint.getEndpointKey()).thenReturn("http://localhost/nonexists");

    // act
    Health subject = HttpHealthIndicators.urlHealthIndicator(endpoint);

    // assert
    assertThat(subject.getStatus()).isEqualTo(Status.DOWN);
  }
}
