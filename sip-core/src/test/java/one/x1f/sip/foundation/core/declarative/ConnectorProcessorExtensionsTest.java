package one.x1f.sip.foundation.core.declarative;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import one.x1f.sip.foundation.core.apps.declarative.ConnectorProcessorExtensionsAdapter;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@AutoConfigureTestRestTemplate
@CamelSpringBootTest
@SpringBootTest(
    classes = {ConnectorProcessorExtensionsAdapter.class},
    properties = {"camel.rest.binding-mode=auto", "camel.openapi.enabled=false"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisableJmx(false)
@MockEndpoints("log:message*")
@DirtiesContext
class ConnectorProcessorExtensionsTest {

  @EndpointInject("mock:log:message")
  private MockEndpoint mockedLogger;

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private ProducerTemplate producerTemplate;

  @LocalServerPort private int localServerPort;

  @BeforeEach
  void setup() {
    mockedLogger.reset();
  }

  @Test
  void
      GIVEN_processor_ordering_specified_WHEN_sending_valid_request_VERIFY_expected_response_order() {

    final var expectedRequestOrder =
        List.of(
            "start",
            "external",
            "dslExt",
            "method",
            "first",
            "second",
            "RestStringAttachmentMapper");
    final var expectedResponseOrder =
        List.of("first", "second", "third", "fourth", "extBean", "end");
    var response =
        producerTemplate.requestBody(
            "direct:" + ConnectorProcessorExtensionsAdapter.INBOUND_DIRECT_OK,
            "start",
            String.class);

    assertThat(response).isNotBlank();
    assertThat(response.split(" ")).containsSequence(expectedRequestOrder);
    assertThat(response.split(" ")).containsSequence(expectedResponseOrder);
  }
}
