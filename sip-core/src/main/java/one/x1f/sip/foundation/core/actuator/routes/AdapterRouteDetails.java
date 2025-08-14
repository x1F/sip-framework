package one.x1f.sip.foundation.core.actuator.routes;

import java.util.Date;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.camel.api.management.mbean.ManagedRouteMBean;
import org.apache.camel.api.management.mbean.RouteError;

/**
 * {@link AdapterRouteDetails} encapsulates all relevant monitoring information about a Camel Route.
 * Internally, it uses Camel's JMX component: @{@link ManagedRouteMBean}.
 */
@Data
public class AdapterRouteDetails {
  private final String id;
  private final String group;
  private final String description;
  private final String status;
  private final long exchangesTotal;
  private final Date startTimestamp;
  private final Date resetTimestamp;
  private final RouteError lastError;
  private final RouteExchangeSummary summary;
  private final RouteProcessingTimes processingTimes;
  private final RouteAdditionalInfo additionalInfo;
  private final String definition;

  /** Constructs the {@link AdapterRouteDetails} object. */
  @SneakyThrows
  public AdapterRouteDetails(ManagedRouteMBean route) {
    this.id = route.getRouteId();
    this.group = route.getRouteGroup();
    this.description = route.getDescription();
    this.status = route.getState();
    this.lastError = route.getLastError();
    this.exchangesTotal = route.getExchangesTotal();
    this.definition = route.dumpRouteAsXml();
    this.startTimestamp = route.getStartTimestamp();
    this.resetTimestamp = route.getResetTimestamp();
    this.summary = summary(route);
    this.processingTimes = processingTimes(route);
    this.additionalInfo = additionalInfo(route);
  }

  private RouteAdditionalInfo additionalInfo(ManagedRouteMBean route) {
    return new RouteAdditionalInfo()
        .setFirstCompletedExchangeId(route.getFirstExchangeCompletedExchangeId())
        .setFirstCompletedExchangeTimestamp(route.getFirstExchangeCompletedTimestamp())
        .setFirstFailureExchangeId(route.getFirstExchangeFailureExchangeId())
        .setFirstFailureExchangeTimestamp(route.getFirstExchangeFailureTimestamp())
        .setLastCompletedExchangeId(route.getLastExchangeCompletedExchangeId())
        .setLastCompletedExchangeTimestamp(route.getLastExchangeCompletedTimestamp())
        .setLastFailureExchangeId(route.getLastExchangeFailureExchangeId())
        .setLastFailureExchangeTimestamp(route.getLastExchangeFailureTimestamp());
  }

  private RouteProcessingTimes processingTimes(ManagedRouteMBean route) throws Exception {
    return new RouteProcessingTimes()
        .setMinProcessingTime(route.getMinProcessingTime())
        .setMaxProcessingTime(route.getMaxProcessingTime())
        .setTotalProcessingTime(route.getTotalProcessingTime())
        .setDeltaProcessingTime(route.getDeltaProcessingTime())
        .setLastProcessingTime(route.getLastProcessingTime())
        .setMeanProcessingTime(route.getMeanProcessingTime());
  }

  private RouteExchangeSummary summary(ManagedRouteMBean route) throws Exception {
    return new RouteExchangeSummary()
        .setExchangesCompleted(route.getExchangesCompleted())
        .setExchangesFailed(route.getExchangesFailed())
        .setExchangesInflight(route.getExchangesInflight())
        .setFailuresHandled(route.getFailuresHandled())
        .setRedeliveries(route.getRedeliveries())
        .setExternalRedeliveries(route.getExternalRedeliveries());
  }

  /** A summary of the Route monitoring information. */
  @Getter
  @Setter
  public static class RouteExchangeSummary {
    private long exchangesCompleted;
    private long exchangesFailed;
    private long exchangesInflight;
    private long failuresHandled;
    private long redeliveries;
    private long externalRedeliveries;
  }

  /** Information about different time processing statistics. */
  @Getter
  @Setter
  public static class RouteProcessingTimes {
    private long minProcessingTime;
    private long maxProcessingTime;
    private long totalProcessingTime;
    private long lastProcessingTime;
    private long deltaProcessingTime;
    private long meanProcessingTime;
  }

  /** Adapter route - Additional information */
  @Getter
  @Setter
  public static class RouteAdditionalInfo {
    private String firstCompletedExchangeId;
    private Date firstCompletedExchangeTimestamp;
    private String firstFailureExchangeId;
    private Date firstFailureExchangeTimestamp;

    private String lastCompletedExchangeId;
    private Date lastCompletedExchangeTimestamp;
    private String lastFailureExchangeId;
    private Date lastFailureExchangeTimestamp;
  }
}
