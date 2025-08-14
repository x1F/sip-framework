package one.x1f.sip.foundation.core.actuator.health.jms;

import static one.x1f.sip.foundation.core.declarative.utils.DeclarativeHelper.appendMetadata;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import java.util.HashMap;
import one.x1f.sip.foundation.core.actuator.common.IntegrationManagementException;
import org.apache.camel.Endpoint;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;

/** JmsHealthIndicators */
public class JmsHealthIndicators {
  private static final Logger logger = LoggerFactory.getLogger(JmsHealthIndicators.class);

  private JmsHealthIndicators() {}

  /**
   * Evaluates health of the {@link JmsEndpoint} based on application ability to start/stop JMS
   * connections
   *
   * @param endpoint {@link Endpoint} singleton
   * @return {@link Health}
   */
  public static Health connectionManageable(Endpoint endpoint) {
    Health.Builder builder = new Health.Builder();
    builder.withDetails(appendMetadata(endpoint, new HashMap<>()));

    if (!(endpoint instanceof JmsEndpoint)) {
      throw new IntegrationManagementException(
          "Invalid endpoint health configuration: "
              + "endpoint is not an instance of the JmsEndpoint: "
              + endpoint);
    }

    JmsEndpoint jmsEndpoint = (JmsEndpoint) endpoint;

    JmsComponent jmsComponent = jmsEndpoint.getComponent();
    ConnectionFactory connectionFactory = jmsComponent.getConnectionFactory();

    try (Connection connection = connectionFactory.createConnection()) {
      logger.debug("Starting JMS connection for the {}", endpoint);
      connection.start();
      connection.stop();
      builder.up();
      logger.debug("Endpoint {} is healthy.", endpoint);
    } catch (JMSException e) {
      logger.warn("Cannot start/stop connection for the {}: {}", endpoint, e);
      builder.down(e);
    }

    return builder.build();
  }
}
