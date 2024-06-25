package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.RouteRole;
import de.ikor.sip.foundation.core.declarative.RoutesRegistry;
import de.ikor.sip.foundation.core.declarative.annonation.InboundConnector;
import de.ikor.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
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

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private class RestParameterMappingProcessor implements Processor {

    final Method mappingMethod;
    final List<Function<Exchange, Object>> parameterFetchers;

    @Override
    public void process(final Exchange exchange) {
      try {
        var args = parameterFetchers.stream().map(fetcher -> fetcher.apply(exchange)).toArray();
        mappingMethod.invoke(RestInboundConnectorBase.this, args);
      } catch (Exception e) {
        throw SIPFrameworkException.init(
            e,
            "Failed to invoke REST parameter-mapper %s in class %s: %s",
            mappingMethod.getName(),
            RestInboundConnectorBase.this.getClass(),
            e.getMessage());
      }
    }
  }

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
}
