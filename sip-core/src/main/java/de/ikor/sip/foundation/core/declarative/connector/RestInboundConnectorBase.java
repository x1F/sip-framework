package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.RouteRole;
import de.ikor.sip.foundation.core.declarative.RoutesRegistry;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import java.util.ArrayList;
import java.util.List;
import org.apache.camel.model.ToDefinition;
import org.apache.camel.model.rest.RestDefinition;
import org.apache.camel.model.rest.RestsDefinition;
import org.apache.camel.model.rest.VerbDefinition;

/**
 * Base class for defining inbound REST connectors via Camel's {@link RestDefinition} DSL.
 *
 * <p>Adapter developers should extend this class and annotate it with @{@link InboundConnector} to
 * specify the connector. The configuration of the REST endpoint is done by overriding the {@link
 * #configureRest(RestDefinition)}.
 *
 * <p>For details on how to handle REST parameter-mappings, refer to {@link ParameterMapping}.
 *
 * @see ParameterMapping
 * @see InboundConnector
 */
public abstract class RestInboundConnectorBase extends InboundConnectorBase
    implements InboundConnectorDefinition<RestsDefinition> {

  @Override
  public final List<String> defineInboundEndpoints(
      final RestsDefinition definition,
      final String targetToBase,
      final RoutesRegistry routeRegistry) {
    var rest = definition.rest();
    var endpointCounter = 0;
    configureRest(rest);
    List<String> routeToPaths = new ArrayList<>();
    for (VerbDefinition verb : rest.getVerbs()) {
      verb.setId(
          routeRegistry.generateRouteIdForConnector(
              RouteRole.EXTERNAL_ENDPOINT, this, "-rest-dsl-", ++endpointCounter));
      String routePath = targetToBase + "-rest-dsl-" + endpointCounter;
      ToDefinition toDefinition = new ToDefinition("direct:" + routePath);
      verb.setTo(toDefinition);
      routeToPaths.add(routePath);
    }
    return routeToPaths;
  }

  /**
   * Configures the REST endpoint used within this connector.
   *
   * <p>Note that while {@link RestDefinition} supports specifying multiple verbs/endpoints, all of
   * those will be mapped to the single integration scenario that this connector is linked with
   * though {@link #getScenarioId()}.
   *
   * @param definition the REST endpoint definition
   */
  protected abstract void configureRest(final RestDefinition definition);

  @Override
  public final Class<RestsDefinition> getEndpointDefinitionTypeClass() {
    return RestsDefinition.class;
  }
}
