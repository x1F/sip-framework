package one.x1f.sip.foundation.core.declarative.connector;

import one.x1f.sip.foundation.core.declarative.ConnectorRegistry;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistry;
import one.x1f.sip.foundation.core.declarative.RouteRole;
import one.x1f.sip.foundation.core.declarative.RoutesRegistry;
import one.x1f.sip.foundation.core.declarative.annotation.InboundConnector;
import one.x1f.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import one.x1f.sip.foundation.core.declarative.dto.ProcessorType;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
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
  public final void defineInboundEndpoints(
      final RestsDefinition definition,
      final String targetToBase,
      final RoutesRegistry routeRegistry,
      final DeclarationsRegistry declarationsRegistry,
      final ConnectorRegistry connectorRegistry) {
    var rest = definition.rest();
    configureRest(rest);
    SIPFrameworkInitializationException.throwIf(
        rest.getVerbs().size() > 1,
        "Using multiple REST endpoints in one Inbound connector is not allowed");
    for (VerbDefinition verb : rest.getVerbs()) {
      String routeId = routeRegistry.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, this);
      verb.setId(routeId);
      ToDefinition toDefinition = new ToDefinition("direct:" + targetToBase);
      verb.setTo(toDefinition);
      connectorRegistry.registerProcessorExtension(
          routeId,
          getId() + "_inbound_entry",
          0,
          verb.asVerb() + ":" + verb.getPath(),
          verb.getPath(),
          ProcessorType.ENTRY);
    }
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
