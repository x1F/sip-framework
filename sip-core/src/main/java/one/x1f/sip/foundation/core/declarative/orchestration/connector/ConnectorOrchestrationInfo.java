package one.x1f.sip.foundation.core.declarative.orchestration.connector;

import java.util.Optional;
import one.x1f.sip.foundation.core.declarative.orchestration.OrchestrationInfo;
import org.apache.camel.model.RouteDefinition;

/**
 * Class that provides the {@link RouteDefinition} handles for a connector to define its necessary
 * orchestrations.
 *
 * <p>The orchestrations done by the connector are typically (but not limited to):
 *
 * <ul>
 *   <li>(Un-)marshalling the domain model used by the orchestrator from/to the message send to the
 *       external system.
 *   <li>Mapping between the connectors own and it's associated integration scenario's request and
 *       response models.
 * </ul>
 *
 * The provided {@link RouteDefinition} also allows for more complex orchestration scenarios with
 * Camel's EIP implementations (e.g. enrich, split, etc.).
 */
public interface ConnectorOrchestrationInfo extends OrchestrationInfo {

  /**
   * {@link RouteDefinition} for the request route of the connector (i.e. before communication with
   * the outbound system(s)).
   *
   * <p>Will typically contain at least the transformation from the connector's to the integration
   * scenario's request model.
   *
   * @return RouteDefiniton handle for the request route within the connector
   */
  RouteDefinition getRequestRouteDefinition();

  /**
   * {@link RouteDefinition} for the response route of the connector (i.e. after receiving the
   * response from the outbound system(s)).
   *
   * <p>Will typically contain at least the transformation from the integration scenario's to the
   * connector's response model.
   *
   * @return RouteDefiniton handle for the response route within the connector. Empty if the
   *     connector or integration scenario does not have a response flow.
   */
  Optional<RouteDefinition> getResponseRouteDefinition();
}
