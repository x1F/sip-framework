package one.x1f.sip.foundation.core.actuator.routes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.springframework.stereotype.Component;

/**
 * Class used by {@link RouteOperation} instead of calling {@link
 * org.apache.camel.spi.RouteController}, in order to add missing logs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RouteControllerLoggingDecorator {
  private final CamelContext ctx;

  /**
   * Starting route and logging the action
   *
   * @param routeId - Id of route to be started
   * @throws Exception - forwarded from {@link
   *     org.apache.camel.spi.RouteController#startRoute(String)}
   */
  public void startRoute(String routeId) throws Exception {
    ctx.getRouteController().startRoute(routeId);
    log.info("Started {} - ({})", routeId, getRouteEndpointUri(routeId));
  }

  /**
   * Stopping route
   *
   * @param routeId - Id of route to be stopped
   * @throws Exception - forwarded from {@link
   *     org.apache.camel.spi.RouteController#stopRoute(String)}
   */
  public void stopRoute(String routeId) throws Exception {
    ctx.getRouteController().stopRoute(routeId);
  }

  /**
   * Suspending route
   *
   * @param routeId - Id of route to be suspended
   * @throws Exception -forwarded from {@link
   *     org.apache.camel.spi.RouteController#suspendRoute(String)}
   */
  public void suspendRoute(String routeId) throws Exception {
    ctx.getRouteController().suspendRoute(routeId);
  }

  /**
   * Resuming route and logging the action
   *
   * @param routeId - Id of route to be resumed
   * @throws Exception - forwarded from {@link
   *     org.apache.camel.spi.RouteController#resumeRoute(String)}
   */
  public void resumeRoute(String routeId) throws Exception {
    ctx.getRouteController().resumeRoute(routeId);
    log.info("Resumed {} - ({})", routeId, getRouteEndpointUri(routeId));
  }

  private String getRouteEndpointUri(String routeId) {
    return ctx.getRoute(routeId).getEndpoint().getEndpointUri();
  }
}
