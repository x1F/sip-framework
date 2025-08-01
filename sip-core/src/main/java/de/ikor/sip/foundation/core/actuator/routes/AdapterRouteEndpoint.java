package de.ikor.sip.foundation.core.actuator.routes;

import de.ikor.sip.foundation.core.declarative.RoutesRegistry;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.CamelContext;
import org.apache.camel.api.management.ManagedCamelContext;
import org.apache.camel.api.management.mbean.ManagedRouteMBean;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Entry point of the HTTP-only Actuator endpoint that exposes management functions from the
 * CamelContext and Camel's JMX MBeans.
 *
 * <p>Among other features, you can use it to list, start and stop Camel routes, as well as to get a
 * plenty of details about each one of them.
 */
@Component
@Endpoint(id = "adapterroutes")
@Slf4j
public class AdapterRouteEndpoint {
  private final CamelContext camelContext;
  private final RouteControllerLoggingDecorator routeController;
  private final ManagedCamelContext mbeanContext;
  private final Optional<RoutesRegistry> routesRegistry;

  /**
   * Route endpoint
   *
   * @param camelContext - CamelContext
   */
  public AdapterRouteEndpoint(
      CamelContext camelContext,
      RouteControllerLoggingDecorator routeController,
      Optional<RoutesRegistry> routesRegistry) {
    this.camelContext = camelContext;
    this.routeController = routeController;
    this.routesRegistry = routesRegistry;
    this.mbeanContext =
        camelContext.getCamelContextExtension().getContextPlugin(ManagedCamelContext.class);
  }

  /**
   * List of routes summaries
   *
   * @return AdapterRouteSummary
   */
  @ReadOperation
  public List<AdapterRouteSummary> routes(@Nullable List<String> ids) {
    if (ids != null && !ids.isEmpty()) {
      return summary(ids);
    } else
      return camelContext.getRoutes().stream()
          .map(route -> generateSummary(route.getRouteId()))
          .toList();
  }

  /**
   * Returns details of a route
   *
   * @param routeId - PathVariable
   * @return AdapterRouteDetails
   */
  @ReadOperation
  public AdapterRouteDetails route(@Selector String routeId) {
    return new AdapterRouteDetails(getRouteMBean(routeId));
  }

  /**
   * Executes a route operation
   *
   * @param routeId - PathVariable
   * @param operation - RouteOperation
   */
  @WriteOperation
  public void execute(@Selector String routeId, @Selector String operation) {
    var operationLowerCase = operation.toLowerCase();
    if ("all".equals(routeId)) {
      switch (operationLowerCase) {
        case "start" -> startAll();
        case "stop" -> stopAll();
        case "resume" -> resumeAll();
        case "suspend" -> suspendAll();
        case "reset" -> resetAll();
        default -> throw SIPFrameworkException.init("Provided operation is not valid");
      }
    } else {
      if ("reset".equals(operationLowerCase)) {
        reset(routeId);
      } else {
        RouteOperation routeOperation = RouteOperation.fromId(operationLowerCase);
        routeOperation.execute(routeController, routeId);
      }
    }
  }

  /** Stops all routes */
  public void stopAll() {
    camelContext
        .getRoutes()
        .forEach(route -> RouteOperation.STOP.execute(routeController, route.getRouteId()));
  }

  /** Resumes all routes */
  public void resumeAll() {
    camelContext
        .getRoutes()
        .forEach(route -> RouteOperation.RESUME.execute(routeController, route.getRouteId()));
  }

  /** Suspends all routes */
  public void suspendAll() {
    camelContext
        .getRoutes()
        .forEach(route -> RouteOperation.SUSPEND.execute(routeController, route.getRouteId()));
  }

  /** Starts all routes */
  public void startAll() {
    this.camelContext
        .getRoutes()
        .forEach(route -> RouteOperation.START.execute(this.routeController, route.getRouteId()));
  }

  /** Resets all routes */
  public void resetAll() {
    camelContext.getRoutes().forEach(route -> getRouteMBean(route.getRouteId()).reset());
  }

  /**
   * Reset a specific route
   *
   * @param routeId - PathVariable
   */
  public void reset(String routeId) {
    getRouteMBean(routeId).reset();
  }

  private ManagedRouteMBean getRouteMBean(String routeId) {
    ManagedRouteMBean routeMBean = mbeanContext.getManagedRoute(routeId);
    if (routeMBean == null) {
      log.warn("sip.core.actuator.routes.routenotfound_{}", routeId);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
    return routeMBean;
  }

  /**
   * Get route summary for provided route ids
   *
   * @param ids list of route ids
   * @return list of {@link AdapterRouteSummary} for provided ids
   */
  public List<AdapterRouteSummary> summary(List<String> ids) {
    return filterRoutesSummary(ids);
  }

  private List<AdapterRouteSummary> filterRoutesSummary(Collection<String> routeIds) {
    return routeIds.stream().map(this::generateSummary).toList();
  }

  private AdapterRouteSummary generateSummary(String routeId) {
    return new AdapterRouteSummary(
        getRouteMBean(routeId),
        routesRegistry.map(registry -> registry.generateRouteInfo(routeId)).orElse(null));
  }
}
