package one.x1f.sip.foundation.core.trace;

import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.NamedRoute;
import org.apache.camel.spi.ExchangeFormatter;
import org.apache.camel.spi.Tracer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@ConditionalOnBooleanProperty(value = "sip.core.tracing.enabled")
@Component
public class CompositeTracer implements Tracer {

  private final List<TraceSupport> tracers;

  /**
   * Creates new instance of CompositeTracer
   *
   * @param tracers list of {@link TraceSupport} available as beans
   */
  public CompositeTracer(List<TraceSupport> tracers) {
    this.tracers = tracers;
  }

  @Override
  public boolean shouldTrace(NamedNode definition) {
    return true;
  }

  @Override
  public void traceBeforeRoute(NamedRoute route, Exchange exchange) {
    filteredTracers(null).forEach(tracer -> tracer.traceBeforeRoute(route, exchange));
  }

  @Override
  public void traceBeforeNode(NamedNode node, Exchange exchange) {
    filteredTracers(node).forEach(tracer -> tracer.traceBeforeNode(node, exchange));
  }

  @Override
  public void traceAfterNode(NamedNode node, Exchange exchange) {
    filteredTracers(node).forEach(tracer -> tracer.traceAfterNode(node, exchange));
  }

  @Override
  public void traceSentNode(NamedNode node, Exchange exchange, Endpoint endpoint, long elapsed) {
    filteredTracers(node)
        .forEach(tracer -> tracer.traceSentNode(node, exchange, endpoint, elapsed));
  }

  @Override
  public void traceAfterRoute(NamedRoute route, Exchange exchange) {
    filteredTracers(null).forEach(tracer -> tracer.traceAfterRoute(route, exchange));
  }

  @Override
  public long getTraceCounter() {
    return 0;
  }

  @Override
  public void resetTraceCounter() {
    // implementation not required
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public void setEnabled(boolean enabled) {
    // always true
  }

  @Override
  public boolean isStandby() {
    return false;
  }

  @Override
  public void setStandby(boolean standby) {
    // always false
  }

  @Override
  public boolean isTraceRests() {
    return false;
  }

  @Override
  public void setTraceRests(boolean traceRests) {
    // implementation not required
  }

  @Override
  public boolean isTraceTemplates() {
    return false;
  }

  @Override
  public void setTraceTemplates(boolean traceTemplates) {
    // implementation not required
  }

  @Override
  public String getTracePattern() {
    return "";
  }

  @Override
  public void setTracePattern(String tracePattern) {
    // implementation not required
  }

  @Override
  public boolean isTraceBeforeAndAfterRoute() {
    return false;
  }

  @Override
  public void setTraceBeforeAndAfterRoute(boolean traceBeforeAndAfterRoute) {
    // implementation not required
  }

  @Override
  public ExchangeFormatter getExchangeFormatter() {
    return null;
  }

  @Override
  public void setExchangeFormatter(ExchangeFormatter exchangeFormatter) {
    // implementation not required
  }

  @Override
  public void start() {
    // implementation not required
  }

  @Override
  public void stop() {
    // implementation not required
  }

  private Stream<TraceSupport> filteredTracers(NamedNode node) {
    return tracers.stream().filter(tracer -> tracer.shouldTrace(node));
  }
}
