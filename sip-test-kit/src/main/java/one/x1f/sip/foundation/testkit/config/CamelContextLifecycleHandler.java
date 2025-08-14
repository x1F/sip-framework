package one.x1f.sip.foundation.testkit.config;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.exception.UnsuspendedRouteException;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import org.apache.camel.*;
import org.apache.camel.spring.boot.CamelContextConfiguration;
import org.apache.camel.support.ScheduledPollConsumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Config class for handling Camel Context by using its lifecycle methods. Used only in SIP batch
 * tests.
 */
@Slf4j
@ConditionalOnProperty(name = "sip.testkit.batch-test", havingValue = "true")
@Component
public class CamelContextLifecycleHandler implements CamelContextConfiguration {

  private final List<RouteInvoker> invokers;

  public CamelContextLifecycleHandler(List<RouteInvoker> invokers) {
    this.invokers = invokers;
  }

  @Override
  public void beforeApplicationStart(CamelContext camelContext) {
    // Do nothing before starting camel context
  }

  @Override
  public void afterApplicationStart(CamelContext camelContext) {
    suspendPollingConsumerRoutes(camelContext);
  }

  private void suspendPollingConsumerRoutes(CamelContext camelContext) {
    camelContext.getRoutes().forEach(route -> checkAndSuspend(route, camelContext));
  }

  private void checkAndSuspend(Route route, CamelContext camelContext) {
    Consumer consumer = route.getConsumer();
    if (shouldSuspendConsumer(consumer)) {
      suspendRoute(route.getRouteId(), camelContext);
    }
  }

  private boolean shouldSuspendConsumer(Consumer consumer) {
    return consumer instanceof PollingConsumer
        || consumer instanceof ScheduledPollConsumer
        || checkRouteInvoker(consumer.getEndpoint());
  }

  private boolean checkRouteInvoker(Endpoint endpoint) {
    return invokers.stream()
        .filter(invoker -> invoker.isApplicable(endpoint))
        .map(invoker -> invoker.shouldSuspend(endpoint))
        .findFirst()
        .orElse(false);
  }

  private void suspendRoute(String routeId, CamelContext camelContext) {
    try {
      camelContext.getRouteController().suspendRoute(routeId);
    } catch (Exception e) {
      camelContext.stop();
      log.debug("sip.testkit.config.nosuspendingroute_{}", routeId);
      throw new UnsuspendedRouteException(routeId);
    }
  }
}
