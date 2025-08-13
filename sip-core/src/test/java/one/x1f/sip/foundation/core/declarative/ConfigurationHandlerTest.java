package one.x1f.sip.foundation.core.declarative;

import static one.x1f.sip.foundation.core.apps.declarative.config.ConfigurationHandlersAdapter.*;
import static one.x1f.sip.foundation.core.declarative.configuration.DeclarativeConfigurationBuilder.ERROR_HANDLER;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.direct;
import static org.assertj.core.api.Assertions.assertThat;

import one.x1f.sip.foundation.core.apps.declarative.config.ConfigurationHandlersAdapter;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.MockEndpoints;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@CamelSpringBootTest
@SpringBootTest(
    classes = {ConfigurationHandlersAdapter.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MockEndpoints("log:message*")
@DirtiesContext
class ConfigurationHandlerTest {

  @EndpointInject("mock:log:message")
  private MockEndpoint mockedLogger;

  @Autowired private FluentProducerTemplate template;

  @Test
  void when_ExceptionIsThrown_with_ConnectorLevelHandler_then_HandledInConnector() {
    Exchange exchange = template.withBody(MESSAGE_IN).to(direct(FAULTY_DIRECT_URI)).send();
    assertThat(exchange.getMessage().getBody(String.class))
        .contains(ConfigurationHandlersAdapter.ConfiguredInConnector.ID);
    assertThat(exchange.getProperty(ERROR_HANDLER, String.class))
        .contains(ConfigurationHandlersAdapter.ConfiguredInConnector.class.getSimpleName());
  }

  @Test
  void when_ExceptionIsThrown_with_ConnectorLevelConfig_then_HandledInConfig() {
    Exchange exchange = template.withBody(MESSAGE_OUT).to(direct(FAULTY_DIRECT_URI)).send();
    assertThat(exchange.getMessage().getBody(String.class)).contains(CUSTOM_DECLARATIVE_CONFIG);
    assertThat(exchange.getProperty(ERROR_HANDLER, String.class))
        .contains(ConfigurationHandlersAdapter.CustomDeclarativeConfig.class.getSimpleName());
  }

  @Test
  void when_ExceptionIsThrown_with_ScenarioLevelConfig_then_HandledInConfig() {
    Exchange exchange = template.withBody(MESSAGE_SCENARIO).to(direct(FAULTY_DIRECT_URI)).send();
    assertThat(exchange.getMessage().getBody(String.class)).contains(SCENARIO_DECLARATIVE_CONFIG);
    assertThat(exchange.getProperty(ERROR_HANDLER, String.class))
        .contains(ConfigurationHandlersAdapter.ScenarioDeclarativeConfig.class.getSimpleName());
  }
}
