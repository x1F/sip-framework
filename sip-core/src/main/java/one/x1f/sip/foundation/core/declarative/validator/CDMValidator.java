package one.x1f.sip.foundation.core.declarative.validator;

import java.util.Objects;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * Utility class that validates if the message body is of the proper type. It validates against the
 * Central Domain Model of the integration scenario.
 *
 * <p><em>Intended for internal use only</em>
 */
public class CDMValidator implements Processor {

  /**
   * Message that is shown when the message from the connector doesn't conform to the Central Domain
   * Model
   */
  public static final String TO_CDM_EXCEPTION_MESSAGE =
      "Data type mismatch in Connector '%s'. Message Type was '%s', but the integration scenario '%s' requires Type '%s'";

  /**
   * Message that is shown when the message from the scenario doesn't conform to the Central Domain
   * Model
   */
  public static final String FROM_CDM_EXCEPTION_MESSAGE =
      "Data type mismatch in Connector '%s'. Message Type was '%s', but the integration scenario '%s' provided Type '%s'";

  private final String scenario;

  private final String connector;
  private final Class<?> centralDomainModel;
  private final String exceptionMessage;

  public CDMValidator(
      String scenario, String connector, Class<?> centralDomainModel, String exceptionMessage) {
    this.scenario = scenario;
    this.connector = connector;
    this.centralDomainModel = centralDomainModel;
    this.exceptionMessage = exceptionMessage;
  }

  @Override
  public void process(Exchange exchange) throws Exception {
    if (!centralDomainModel.isInstance(exchange.getMessage().getBody())) {
      throw SIPFrameworkException.init(
          exceptionMessage,
          connector,
          Objects.nonNull(exchange.getMessage().getBody())
              ? exchange.getMessage().getBody().getClass().getName()
              : "NULL",
          scenario,
          centralDomainModel.getName());
    }
  }
}
