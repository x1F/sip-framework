package one.x1f.sip.foundation.testkit.workflow.givenphase;

import static one.x1f.sip.foundation.testkit.util.TestKitHelper.unmarshallExchangeBodyFromJson;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvoker.CONNECTOR_ID_EXCHANGE_PROPERTY;
import static one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl.DirectRouteInvoker.STRING_CLASS;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistryApi;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;
import one.x1f.sip.foundation.core.proxies.ProcessorProxy;
import one.x1f.sip.foundation.core.proxies.ProcessorProxyRegistry;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import one.x1f.sip.foundation.testkit.exception.ExceptionType;
import one.x1f.sip.foundation.testkit.exception.TestCaseInitializationException;
import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
import org.apache.camel.Exchange;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Creates and defines behaviour for Camel based external service mocks */
@Slf4j
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ProcessorProxyMock extends Mock {
  private ProcessorProxy proxy;
  private final ProcessorProxyRegistry proxyRegistry;
  private final ObjectMapper mapper;
  private final Optional<DeclarationsRegistryApi> declarationsRegistry;

  /**
   * Sets a mock operation on a proxy
   *
   * @param testExecutionStatus that the mock should fill with details of the test run
   */
  @Override
  public void setBehavior(TestExecutionStatus testExecutionStatus) {
    proxy = getMockProxy(getId());
    proxy.mock(this.createOperation(returnExchange));
  }

  @Override
  public void clear() {
    // If execution is stopped by error before proxy is created, proxy reference can point to null
    if (proxy != null) {
      proxy.reset();
      proxy.mock(exchange -> exchange);
    }
  }

  private ProcessorProxy getMockProxy(String processorId) {
    return proxyRegistry
        .getProxy(processorId)
        .orElseThrow(
            () ->
                new TestCaseInitializationException(
                    "There is no " + processorId + " proxy in the application",
                    ExceptionType.MOCK));
  }

  private UnaryOperator<Exchange> createOperation(Exchange returnExchange) {
    return exchange -> {
      returnExchange.getMessage().getHeaders().forEach(exchange.getMessage()::setHeader);
      String mockingPayload = returnExchange.getMessage().getBody(String.class);
      exchange.getMessage().setBody(mockingPayload);

      String connectorId = (String) returnExchange.getProperty(CONNECTOR_ID_EXCHANGE_PROPERTY);
      if (connectorId != null && declarationsRegistry.isPresent()) {
        unmarshallFromJson(exchange, connectorId);
      }

      return exchange;
    };
  }

  private void unmarshallFromJson(Exchange exchange, String connectorId) {
    Optional<ConnectorDefinition> connector =
        declarationsRegistry.flatMap(registry -> registry.getConnectorById(connectorId));
    if (connector.isPresent()) {
      Optional<Class<?>> responseModelClass = connector.get().getResponseModelClass();
      if (responseModelClass.isPresent()) {
        if (!responseModelClass.get().equals(STRING_CLASS)) {
          unmarshallExchangeBodyFromJson(exchange, mapper, responseModelClass.get());
        }
      } else {
        throw SIPFrameworkException.init(
            "Response model class is not defined for connector: %s", connectorId);
      }
    }
  }
}
