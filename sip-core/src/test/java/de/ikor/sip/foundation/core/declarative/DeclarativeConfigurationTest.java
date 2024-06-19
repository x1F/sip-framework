package de.ikor.sip.foundation.core.declarative;

import static de.ikor.sip.foundation.core.apps.declarative.config.FaultyAdapter.*;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.Assertions.assertThat;

import de.ikor.sip.foundation.core.apps.declarative.config.FaultyAdapter;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.DisableJmx;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(
    classes = {FaultyAdapter.class},
    properties = {"camel.rest.binding-mode=auto", "camel.openapi.enabled=false"},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisableJmx(false)
@MockEndpoints("log:message*")
@DirtiesContext
class DeclarativeConfigurationTest {

  @EndpointInject("mock:log:message")
  private MockEndpoint mockedLogger;

  @Autowired private FluentProducerTemplate template;
  @LocalServerPort private int localServerPort;

  @BeforeEach
  void setup() {
    mockedLogger.reset();
  }

  @AfterEach
  void assertTest() throws InterruptedException {
    mockedLogger.assertIsSatisfied();
  }

  @Test
  void when_AppendingScenarioSendMessage_then_AdapterOutputsIt() {
    Exchange exchange = template.withBody(MESSAGE_IN).to(direct(FAULTY_DIRECT_URI)).send();
    assertThat(exchange.getMessage().getBody(String.class)).contains("Handled in Connector");
  }

  @Test
  void when_AppendingScenarioSendMessage_then_AdapterOutputsIt2() {
    Exchange exchange = template.withBody(MESSAGE_OUT).to(direct(FAULTY_DIRECT_URI)).send();
    assertThat(exchange.getMessage().getBody(String.class))
        .contains("Handled by CustomDeclarativeConfig");
  }
}
