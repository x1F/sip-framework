package one.x1f.sip.foundation.core.actuator.routes;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import one.x1f.sip.foundation.core.declarative.dto.RouteDeclarativeStructureInfo;
import org.apache.camel.api.management.mbean.ManagedRouteMBean;

/** Provides a brief summary for the Camel Route. */
@Getter
@Setter
public class AdapterRouteSummary {
  private String id;
  private String state;
  private long exchangesTotal;
  private long exchangesCompleted;
  private long exchangesFailed;
  private long exchangesInflight;

  @JsonInclude(Include.NON_NULL)
  private RouteDeclarativeStructureInfo routeDeclarativeStructureInfo;

  /**
   * Initializes an AdapterRouteSummary
   *
   * @param managedRoute {@link ManagedRouteMBean}
   */
  @SneakyThrows
  public AdapterRouteSummary(
      ManagedRouteMBean managedRoute, RouteDeclarativeStructureInfo routeDeclarativeStructureInfo) {
    this.id = managedRoute.getRouteId();
    this.state = managedRoute.getState();
    this.exchangesTotal = managedRoute.getExchangesTotal();
    this.exchangesCompleted = managedRoute.getExchangesCompleted();
    this.exchangesFailed = managedRoute.getExchangesFailed();
    this.exchangesInflight = managedRoute.getExchangesInflight();
    this.routeDeclarativeStructureInfo = routeDeclarativeStructureInfo;
  }
}
