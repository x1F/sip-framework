package de.ikor.sip.foundation.core.declarative.orchestration.connector;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.model.RouteDefinition;

/**
 * Standard implementation for orchestrating connectors.
 *
 * <p>This relies on the connector developer to implement the necessary orchestration steps for the
 * connector (such as data model transformation) using Camel's DSL from the provided {@link
 * RouteDefinition} handles.
 *
 * @see ConnectorOrchestrationInfo
 * @deprecated Use {@link ConnectorProcessorChainOrchestrator} based approach instead
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Accessors(chain = true)
@Deprecated(since = "3.4.0")
public class ConnectorOrchestrator implements Orchestrator<ConnectorOrchestrationInfo> {
  private final Supplier<ConnectorDefinition> relatedConnector;
  private Consumer<RouteDefinition> requestRouteTransformer = null;
  private Consumer<RouteDefinition> responseRouteTransformer = this::defaultResponseTransformer;

  /**
   * @deprecated Use {@link ConnectorProcessorChainOrchestrator} based approach instead
   */
  public static ConnectorOrchestrator forConnector(final ConnectorDefinition relatedConnector) {
    return new ConnectorOrchestrator(() -> relatedConnector);
  }

  private void defaultResponseTransformer(final RouteDefinition definition) {
    ConnectorDefinition connector = relatedConnector.get();
    connector
        .getResponseModelClass()
        .ifPresent(
            aClass ->
                log.warn(
                    "No response transformation definition has been assigned to connector '{}' in connector-class '{}'",
                    connector.getId(),
                    connector.getClass().getName()));

    definition.process(exchange -> {});
  }

  @Override
  public boolean canOrchestrate(final ConnectorOrchestrationInfo info) {
    return true;
  }

  @Override
  public void doOrchestrate(final ConnectorOrchestrationInfo info) {
    buildRequestRouteTransformer(info);
    info.getResponseRouteDefinition().ifPresent(responseRouteTransformer);
  }

  private void buildRequestRouteTransformer(ConnectorOrchestrationInfo info) {
    if (requestRouteTransformer == null) {
      log.warn(
          "No request transformation definition has been assigned to connector '{}' in connector-class '{}'",
          relatedConnector.get().getId(),
          relatedConnector.get().getClass().getName());
    } else {
      requestRouteTransformer.accept(info.getRequestRouteDefinition());
    }
  }
}
