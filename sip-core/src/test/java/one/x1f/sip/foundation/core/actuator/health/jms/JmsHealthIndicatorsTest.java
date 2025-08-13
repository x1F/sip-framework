package one.x1f.sip.foundation.core.actuator.health.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import one.x1f.sip.foundation.core.actuator.common.IntegrationManagementException;
import org.apache.camel.Endpoint;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Status;

class JmsHealthIndicatorsTest {

  private JmsEndpoint endpoint;
  private ConnectionFactory connectionFactory;

  @BeforeEach
  void setUp() {
    endpoint = mock(JmsEndpoint.class, RETURNS_DEEP_STUBS);
    JmsComponent component = mock(JmsComponent.class);
    connectionFactory = mock(ConnectionFactory.class);

    when(endpoint.getComponent()).thenReturn(component);
    when(component.getConnectionFactory()).thenReturn(connectionFactory);
  }

  @Test
  void Given_ConnectionCreated_When_connectionManageable_Then_StatusUp() throws JMSException {
    // arrange
    Connection connection = mock(Connection.class);
    when(connectionFactory.createConnection()).thenReturn(connection);

    // assert
    assertThat(JmsHealthIndicators.connectionManageable(endpoint).getStatus()).isEqualTo(Status.UP);
  }

  @Test
  void Given_CreateConnectionThrowsException_When_connectionManageable_Then_StatusDown()
      throws JMSException {
    // arrange
    when(connectionFactory.createConnection()).thenThrow(JMSException.class);

    // assert
    assertThat(JmsHealthIndicators.connectionManageable(endpoint).getStatus())
        .isEqualTo(Status.DOWN);
  }

  @Test
  void Given_IncorrectEndpoint_When_connectionManageable_Then_IntegrationManagementException() {
    // arrange
    Endpoint genericEndpoint = mock(Endpoint.class, RETURNS_DEEP_STUBS);
    // assert
    assertThatThrownBy(() -> JmsHealthIndicators.connectionManageable(genericEndpoint))
        .isInstanceOf(IntegrationManagementException.class);
  }
}
