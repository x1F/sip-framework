package one.x1f.sip.foundation.core.trace;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.NamedNode;
import org.apache.camel.NamedRoute;

public interface TraceSupport {
  void traceBeforeNode(NamedNode node, Exchange exchange);

  void traceAfterNode(NamedNode node, Exchange exchange);

  void traceBeforeRoute(NamedRoute route, Exchange exchange);

  void traceAfterRoute(NamedRoute route, Exchange exchange);

  void traceSentNode(NamedNode node, Exchange exchange, Endpoint endpoint, long elapsed);

  boolean shouldTrace(NamedNode node);
}
