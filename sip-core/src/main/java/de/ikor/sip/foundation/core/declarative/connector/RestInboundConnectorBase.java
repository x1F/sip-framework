package de.ikor.sip.foundation.core.declarative.connector;

import static de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper.appendOnException;
import static de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper.joinConfigurationIds;

import de.ikor.sip.foundation.core.declarative.AdapterBuilder;
import de.ikor.sip.foundation.core.declarative.RouteRole;
import de.ikor.sip.foundation.core.declarative.RoutesRegistry;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import org.apache.camel.builder.EndpointProducerBuilder;
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
      final EndpointProducerBuilder targetToDefinition,
      final RoutesRegistry routeRegistry) {
    var rest = definition.rest();
    var endpointCounter = 0;
    configureRest(rest);
    for (VerbDefinition verb : rest.getVerbs()) {
      verb.setId(
          routeRegistry.generateRouteIdForConnector(
              RouteRole.EXTERNAL_ENDPOINT, this, ++endpointCounter));
      verb.setTo(new ToDefinition(targetToDefinition));
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

  private List<Method> initializeParameterMapperMethods() {
    return Arrays.stream(getClass().getMethods())
        .filter(method -> method.isAnnotationPresent(ParameterMapping.class))
        .toList();
  }

  private EndpointProducerBuilder bindParameterMapperMethods(
      final CamelContext camelContext,
      final EndpointProducerBuilder targetToDefinition,
      final RoutesRegistry routeRegistry) {
    final var mappingMethods = initializeParameterMapperMethods();
    if (mappingMethods.isEmpty()) {
      return targetToDefinition;
    }
    final var defs = camelContext.getRegistry().mandatoryFindSingleByType(AdapterBuilder.class);
    final var routeId =
        routeRegistry.generateRouteIdForConnector(RouteRole.CONNECTOR_REST_PARAMETER_MAPPING, this);
    final var mapperInterceptor = StaticEndpointBuilders.direct(routeId);
    final var routeDef = defs.from(mapperInterceptor).routeId(routeId);
    mappingMethods.forEach(method -> routeDef.process(buildParameterMappingProcessor(method)));
    routeDef.to(targetToDefinition);
    return mapperInterceptor;
  }

  private RestParameterMappingProcessor buildParameterMappingProcessor(final Method mappingMethod) {
    final List<Function<Exchange, Object>> parameterFetchers = new ArrayList<>();
    for (var param : mappingMethod.getParameters()) {
      if (param.isAnnotationPresent(PathParameter.class)) {
        parameterFetchers.add(
            exchange ->
                exchange
                    .getMessage()
                    .getHeader(param.getAnnotation(PathParameter.class).value(), param.getType()));
      } else if (param.isAnnotationPresent(QueryParameter.class)) {
        parameterFetchers.add(
            exchange ->
                exchange
                    .getMessage()
                    .getHeader(param.getAnnotation(QueryParameter.class).value(), param.getType()));
      } else if (param.getType().equals(Exchange.class)) {
        parameterFetchers.add(exchange -> exchange);
      } else if (param.getType().equals(Message.class)) {
        parameterFetchers.add(Exchange::getMessage);
      } else {
        parameterFetchers.add(exchange -> exchange.getMessage().getBody(param.getType()));
      }
    }
    return new RestParameterMappingProcessor(
        mappingMethod, Collections.unmodifiableList(parameterFetchers));
  }
}
