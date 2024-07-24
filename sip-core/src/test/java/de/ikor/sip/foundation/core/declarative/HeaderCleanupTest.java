package de.ikor.sip.foundation.core.declarative;

import static org.assertj.core.api.Assertions.assertThat;

import de.ikor.sip.foundation.core.apps.declarative.HeaderCleanupAdapter;
import java.util.List;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.camel.test.spring.junit5.MockEndpoints;
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
@MockEndpoints("stub:*")
@DirtiesContext
class HeaderCleanupTest {

  @Autowired private TestRestTemplate restTemplate;

  @EndpointInject("mock:stub:http://somehost.here/header-test")
  private MockEndpoint mockedOutbound;

  @LocalServerPort private int localServerPort;

  @Test
  void
      GIVEN_request_with_headers_WHEN_calling_connector_with_header_cleaning_VERIFY_headers_are_reinstated()
          throws InterruptedException {

    HttpHeaders headers = new HttpHeaders();
    headers.add("firstHeader", "first");
    headers.add("secondHeader", "second");
    headers.add("secondary", "third");
    headers.add("firstHiddenKey", "hidden1");
    headers.add("secondHiddenKey", "hidden2");

    mockedOutbound.whenAnyExchangeReceived(
        exchange -> {
          assertThat(exchange.getMessage().getHeaders()).doesNotContainKey("firstHeader");
          assertThat(exchange.getMessage().getHeaders()).doesNotContainKey("firstHiddenKey");
          assertThat(exchange.getMessage().getHeaders()).containsEntry("secondHeader", "second");
          assertThat(exchange.getMessage().getHeaders()).containsEntry("secondary", "third");
          assertThat(exchange.getMessage().getHeaders())
              .containsEntry("secondHiddenKey", "hidden2");
          exchange.getMessage().setHeader("firstHeader", "firstModified");
        });

    final var response =
        restTemplate.exchange(
            "/adapter/test", HttpMethod.GET, new HttpEntity<>(headers), String.class);

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getHeaders()).containsEntry("firstHeader", List.of("firstModified"));
    assertThat(response.getHeaders()).containsEntry("secondHeader", List.of("second"));
    assertThat(response.getHeaders()).containsEntry("secondary", List.of("third"));
    assertThat(response.getHeaders()).containsEntry("firstHiddenKey", List.of("hidden1"));
    assertThat(response.getHeaders()).containsEntry("secondHiddenKey", List.of("hidden2"));

    mockedOutbound.assertIsSatisfied();
  }
}
