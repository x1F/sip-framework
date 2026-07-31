package one.x1f.sip.foundation.core.trace;

import static one.x1f.sip.foundation.core.util.SIPExchangeHelper.extractBodyAsJsonString;

import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.ConnectorRegistry;
import one.x1f.sip.foundation.core.declarative.RoutesRegistry;
import one.x1f.sip.foundation.core.declarative.dto.ProcessorType;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.NamedRoute;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@ConditionalOnBooleanProperty(value = "sip.core.tracing.logging.connector")
@Service
public class ConnectorTracer implements TraceSupport {

  private final RoutesRegistry routesRegistry;
  private final ConnectorRegistry connectorRegistry;

  @Value("${sip.core.tracing.exchange-formatter.showExchangeId:false}")
  private boolean showExchangeId;

  @Value("${sip.core.tracing.exchange-formatter.showBody:false}")
  private boolean showBody;

  @Value("${sip.core.tracing.exchange-formatter.showHeaders:false}")
  private boolean showHeaders;

  @Override
  public void traceBeforeNode(NamedNode node, Exchange exchange) {}

  @Override
  public void traceAfterNode(NamedNode node, Exchange exchange) {}

  @Override
  public void traceSentNode(NamedNode node, Exchange exchange, Endpoint endpoint, long elapsed) {}

  @Override
  public void traceBeforeRoute(NamedRoute route, Exchange exchange) {
    logRouteStep("Entering Connector", route, exchange);
  }

  @Override
  public void traceAfterRoute(NamedRoute route, Exchange exchange) {
    logRouteStep("Leaving Connector", route, exchange);
  }

  @Override
  public boolean shouldTrace(NamedNode node) {
    return true;
  }

  private void logRouteStep(String prefix, NamedRoute route, Exchange exchange) {
    var routeInfo = routesRegistry.generateRouteInfo(route.getRouteId());
    var processorInfo = connectorRegistry.getProcessorExtensions(route.getRouteId());

    processorInfo.stream()
        .filter(info -> info.getType() == ProcessorType.REQUEST)
        .findFirst()
        .ifPresent(
            value -> {
              var scenarioId = routeInfo.getScenarioId();
              var connectorId = routeInfo.getConnectorId();
              var exchangeId = exchange.getExchangeId();
              var headers =
                  exchange.getMessage().getHeaders().entrySet().stream()
                      .map(entry -> entry.getKey() + ": " + entry.getValue())
                      .collect(Collectors.joining(", "));

              String details =
                  Stream.of(
                          "scenarioId=" + scenarioId,
                          "connectorId=" + connectorId,
                          showExchangeId ? "exchangeId=" + exchangeId : null,
                          showBody
                              ? "body=" + extractBodyAsJsonString(exchange.getMessage())
                              : null,
                          showHeaders ? "headers=" + headers : null)
                      .filter(Objects::nonNull)
                      .collect(Collectors.joining(", "));

              log.info("{} [{}]", prefix, details);
            });
  }
}
