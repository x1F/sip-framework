package de.ikor.sip.foundation.core.declarative;

import static org.assertj.core.api.Assertions.assertThat;

import de.ikor.sip.foundation.core.apps.declarative.RestParamMappingAdapter;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(
    classes = {RestParamMappingAdapter.class},
    properties = {"camel.rest.binding-mode=auto", "camel.openapi.enabled=false"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisableJmx(false)
@MockEndpoints("log:message*")
@DirtiesContext
class RestParamMappingTest {

  @EndpointInject("mock:log:message")
  private MockEndpoint mockedLogger;

  @Autowired private TestRestTemplate restTemplate;

  @LocalServerPort private int localServerPort;

  @BeforeEach
  void setup() {
    mockedLogger.reset();
  }

  @Test
  void GIVEN_valid_request_WHEN_calling_rest_with_param_mappings_VERIFY_result_object_is_mapped() {

    final var response =
        restTemplate.getForEntity(
            "/adapter/mapper/42/test?query=query-test",
            RestParamMappingAdapter.RestMappedData.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody().getPathParamFirst()).isEqualTo(42);
    assertThat(response.getBody().getPathParamSecond()).isEqualTo("test");
    assertThat(response.getBody().getQueryParam()).isEqualTo("query-test");
    assertThat(response.getBody().getBody()).isBlank();
  }

  @Test
  void GIVEN_invalid_request_WHEN_calling_rest_with_param_mappings_VERIFY_exception_is_triggered() {
    final var response =
        restTemplate.getForEntity(
            "/adapter/mapper/shouldBeANumber/test?query=query-test",
            RestParamMappingAdapter.RestMappedData.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode().is5xxServerError()).isTrue();
  }
}
