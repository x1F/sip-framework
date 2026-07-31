package one.x1f.sip.foundation.core.trace;

import static one.x1f.sip.foundation.core.util.SIPExchangeHelper.extractBodyAsJsonString;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.connector.MethodBasedConnectorProcessor;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.NamedRoute;
import org.apache.camel.model.ProcessDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;

@Slf4j
@ConditionalOnBooleanProperty(value = "sip.core.tracing.logging.connector-extension")
@Service
public class ConnectorExtensionTracer implements TraceSupport {

  @Value("${sip.core.tracing.exchange-formatter.showExchangeId:false}")
  private boolean showExchangeId;

  @Value("${sip.core.tracing.exchange-formatter.showBody:false}")
  private boolean showBody;

  @Value("${sip.core.tracing.exchange-formatter.showHeaders:false}")
  private boolean showHeaders;

  @Override
  public void traceBeforeNode(NamedNode node, Exchange exchange) {
    logNodeStep("Entering Connector Extension", node, exchange);
  }

  @Override
  public void traceAfterNode(NamedNode node, Exchange exchange) {
    logNodeStep("Leaving Connector Extension", node, exchange);
  }

  @Override
  public void traceSentNode(NamedNode node, Exchange exchange, Endpoint endpoint, long elapsed) {}

  @Override
  public void traceBeforeRoute(NamedRoute route, Exchange exchange) {}

  @Override
  public void traceAfterRoute(NamedRoute route, Exchange exchange) {}

  @Override
  public boolean shouldTrace(NamedNode node) {
    return true;
  }

  private Optional<MethodBasedConnectorProcessor> getMethodBasedConnectorProcessor(NamedNode node) {
    if (!(node instanceof ProcessDefinition processDefinition)) {
      return Optional.empty();
    }

    var processor = processDefinition.getProcessor();
    if (!(processor instanceof MethodBasedConnectorProcessor connectorProcessor)) {
      return Optional.empty();
    }

    return Optional.of(connectorProcessor);
  }

  private void logNodeStep(String prefix, NamedNode node, Exchange exchange) {
    var processor = getMethodBasedConnectorProcessor(node);
    processor.ifPresent(
        value -> {
          var scenarioId = value.getConnector().getScenarioId();
          var connectorId = value.getConnector().getId();
          var extensionName = value.getExtensionName();
          var annotations =
              Arrays.stream(processor.get().getProcessorMethod().getDeclaredAnnotations())
                  .map(annotation -> annotation.annotationType().getSimpleName())
                  .collect(Collectors.joining(","));
          var exchangeId = exchange.getExchangeId();
          var headers =
              exchange.getMessage().getHeaders().entrySet().stream()
                  .map(entry -> entry.getKey() + ": " + entry.getValue())
                  .collect(Collectors.joining(", "));

          String details =
              Stream.of(
                      "scenarioId=" + scenarioId,
                      "connectorId=" + connectorId,
                      "extensionName=" + extensionName,
                      "annotations=" + annotations,
                      showExchangeId ? "exchangeId=" + exchangeId : null,
                      showBody ? "body=" + extractBodyAsJsonString(exchange.getMessage()) : null,
                      showHeaders ? "headers=" + headers : null)
                  .filter(Objects::nonNull)
                  .collect(Collectors.joining(", "));

          log.info("{} [{}]", prefix, details);
        });
  }
}
