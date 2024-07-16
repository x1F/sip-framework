package de.ikor.sip.foundation.core.declarative;

import static org.assertj.core.api.Assertions.assertThat;

import de.ikor.sip.foundation.core.apps.declarative.HeaderCleanupAdapter;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(
    classes = {HeaderCleanupAdapter.class},
    properties = {"camel.rest.binding-mode=auto", "camel.openapi.enabled=false"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisableJmx(false)
@DirtiesContext
class HeaderCleanupTest {

  @Autowired private TestRestTemplate restTemplate;

  @LocalServerPort private int localServerPort;

  @Test
  void
      GIVEN_request_with_headers_WHEN_calling_connector_with_header_cleaning_VERIFY_headers_are_reinstated() {

    HttpHeaders headers = new HttpHeaders();
    headers.add("firstHeader", "first");
    headers.add("secondHeader", "second");
    headers.add("secondary", "third");

    final var response =
        restTemplate.exchange(
            "/adapter/test", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getHeaders()).containsKey("firstHeader");
    assertThat(response.getHeaders()).containsKey("secondHeader");
    assertThat(response.getHeaders()).containsKey("secondary");
  }
}
