package de.ikor.sip.foundation.core.declarative.utils;

import de.ikor.sip.foundation.core.declarative.RouteRole;
import de.ikor.sip.foundation.core.declarative.RoutesRegistry;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorType;
import de.ikor.sip.foundation.core.declarative.model.ModelMapper;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import org.apache.camel.Endpoint;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.endpoint.dsl.JmsEndpointBuilderFactory;
import org.mapstruct.factory.Mappers;

/**
 * Helper methods for the declarative structure adapter building.
 *
 * <p><em>Intended for internal use only</em>
 */
@UtilityClass
public class DeclarativeHelper {

  public static final String CONNECTOR_ID_FORMAT = "%s-%s-%s";
  private static final Pattern DOLLAR_PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{[^}]+\\}");
  private static final Pattern DOUBLE_CURLY_PLACEHOLDER_PATTERN =
      Pattern.compile("\\{\\{[^}]+\\}\\}");

  public static String formatConnectorId(
      ConnectorType type, String scenarioID, String connectorGroupID) {
    return String.format(CONNECTOR_ID_FORMAT, type.getValue(), scenarioID, connectorGroupID);
  }

  public static <T extends ModelMapper> T createMapperInstance(Class<T> clazz) {
    try {
      return Mappers.getMapper(clazz);
    } catch (RuntimeException e) {
      // swallow the exception, it's not a mapstruct mapper
      try {
        return DeclarativeReflectionUtils.createInstance(clazz);
      } catch (NoSuchMethodException ex) {
        throw SIPFrameworkInitializationException.init(
            "Mapper %s needs to have a no-arg constructor, please define one.", clazz.getName());
      } catch (InvocationTargetException
          | InstantiationException
          | IllegalAccessException exception) {
        throw SIPFrameworkInitializationException.init(
            exception, "SIP couldn't create a Mapper %s.", clazz.getName());
      }
    }
  }

  /**
   * Append metadata to existing map of details needed in health check
   *
   * @param endpoint Endpoint for which matadata is needed
   * @param details Map with details
   * @return Map with filled metadata details
   */
  public static Map<String, Object> appendMetadata(Endpoint endpoint, Map<String, Object> details) {
    getRoutesRegistry(endpoint)
        .ifPresent(
            routesRegistry ->
                details.put("metadata", routesRegistry.generateRouteInfoList(endpoint)));
    return details;
  }

  private static Optional<RoutesRegistry> getRoutesRegistry(Endpoint endpoint) {
    RoutesRegistry routesRegistry =
        endpoint
            .getCamelContext()
            .getRegistry()
            .lookupByNameAndType("routesRegistry", RoutesRegistry.class);
    return Optional.ofNullable(routesRegistry);
  }

  public static EndpointConsumerBuilder resolveForbiddenEndpoint(
      EndpointConsumerBuilder endpointConsumerBuilder) {
    if (endpointConsumerBuilder instanceof JmsEndpointBuilderFactory.JmsEndpointBuilder)
      endpointConsumerBuilder.doSetProperty("bridgeErrorHandler", false);
    return endpointConsumerBuilder;
  }

  public static Method getMappingMethod(Class<?> clazz) {
    List<Method> candidateMethods =
        Arrays.stream(clazz.getDeclaredMethods())
            .filter(method -> method.getName().equals(ModelMapper.MAPPING_METHOD_NAME))
            .filter(method -> !method.isBridge())
            .filter(method -> method.getParameterTypes().length == 1)
            .toList();
    if (candidateMethods.size() != 1) {
      throw SIPFrameworkInitializationException.init(
          "Failed to automatically resolve the model classes for the Mapper: %s. Please @Override the getSourceModelClass() and getTargetModelClass() methods",
          clazz.getName());
    }
    return candidateMethods.get(0);
  }

  public static boolean isPrimaryEndpoint(ConnectorType type, String role) {
    return isInboundPrimaryEndpoint(type, role) || isOutboundPrimaryEndpoint(type, role);
  }

  private static boolean isInboundPrimaryEndpoint(ConnectorType type, String role) {
    return type.equals(ConnectorType.IN)
        && (role.equals(RouteRole.EXTERNAL_ENDPOINT.getExternalName())
            || role.equals(RouteRole.EXTERNAL_SOAP_SERVICE_PROXY.getExternalName()));
  }

  private static boolean isOutboundPrimaryEndpoint(ConnectorType type, String role) {
    return type.equals(ConnectorType.OUT)
        && (role.equals(RouteRole.EXTERNAL_ENDPOINT.getExternalName()));
  }

  /**
   * Checks whether provided string URI contains any placeholders
   *
   * @param uri URI to be checked
   * @return true if URI contains placeholders
   */
  public static boolean doesUriContainPlaceholders(String uri) {
    Matcher dollarMatcher = DOLLAR_PLACEHOLDER_PATTERN.matcher(uri);
    if (dollarMatcher.find()) {
      return true;
    }
    Matcher doubleCurlyMatcher = DOUBLE_CURLY_PLACEHOLDER_PATTERN.matcher(uri);
    if (doubleCurlyMatcher.find()) {
      return true;
    }

    return false;
  }
}
