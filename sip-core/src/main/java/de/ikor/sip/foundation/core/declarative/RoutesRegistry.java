package de.ikor.sip.foundation.core.declarative;

import static de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper.isPrimaryEndpoint;
import static de.ikor.sip.foundation.core.util.CamelProcessorsHelper.getEndpointUri;
import static de.ikor.sip.foundation.core.util.CamelProcessorsHelper.isInMemoryUri;

import de.ikor.sip.foundation.core.actuator.declarative.model.EndpointInfo;
import de.ikor.sip.foundation.core.actuator.declarative.model.RouteDeclarativeStructureInfo;
import de.ikor.sip.foundation.core.actuator.declarative.model.RouteInfo;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.proxies.ProcessorProxy;
import de.ikor.sip.foundation.core.proxies.ProcessorProxyRegistry;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Synchronized;
import org.apache.camel.*;
import org.apache.camel.component.servlet.ServletConsumer;
import org.apache.camel.processor.Enricher;
import org.apache.camel.processor.PollEnricher;
import org.apache.camel.spi.CamelEvent;
import org.apache.camel.spi.CamelEvent.CamelContextEvent;
import org.apache.camel.spi.CamelEvent.CamelContextStartedEvent;
import org.apache.camel.spi.IdAware;
import org.apache.camel.support.SimpleEventNotifierSupport;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Internal registry that holds mapping between declarative elements and Camel Routes.
 *
 * <p><em>For internal use only</em>
 */
@Service
public class RoutesRegistry extends SimpleEventNotifierSupport {

  private static final String ENRICH = "enrich";
  private static final String POLL_ENRICH = "pollEnrich";
  public static final String SIP_CONNECTOR_PREFIX = "sip-connector";
  public static final String SIP_SOAP_SERVICE_PREFIX = "sip-soap-service";
  public static final String SIP_SCENARIO_ORCHESTRATOR_PREFIX = "sip-scenario";

  public static final String SIP_COMPOSITE_ORCHESTRATOR_PREFIX = "sip-process";
  private final DeclarationsRegistryApi declarationsRegistryApi;

  private final MultiValuedMap<ConnectorDefinition, String> routeIdsForConnectorRegister =
      new HashSetValuedHashMap<>();

  private final Map<String, ConnectorDefinition> connectorForRouteIdRegister = new HashMap<>();
  private final Map<String, String> routeIdForSoapServiceRegister = new HashMap<>();
  private final Map<String, RouteRole> roleForRouteIdRegister = new HashMap<>();
  private final MultiValuedMap<String, String> endpointsForRouteId = new HashSetValuedHashMap<>();
  private final MultiValuedMap<String, String> routeIdsForEndpoints = new HashSetValuedHashMap<>();

  // Map of outgoing endpoints and their processor ids
  private final Map<String, IdAware> outgoingEndpointIds = new HashMap<>();

  // Counter for creating unified endpoint uri for enrich processor which have no expression
  private int enrichCounter = 1;

  // Counter for creating unified endpoint uri for pollEnrich processor which have no expression
  private int pollEnrichCounter = 1;

  public RoutesRegistry(DeclarationsRegistryApi declarationsRegistryApi) {
    this.declarationsRegistryApi = declarationsRegistryApi;
  }

  /** On CamelContextStartedEvent execute this class's event listener - notify() */
  @Override
  public boolean isEnabled(CamelEvent event) {
    return event instanceof CamelContextStartedEvent;
  }

  /** Trigger caching of routes and endpoints mappings */
  @Override
  public void notify(CamelEvent event) {
    prefillEndpointRouteMappings(((CamelContextEvent) event).getContext());
  }

  @Synchronized
  public String generateRouteIdForConnector(
      final RouteRole role, final ConnectorDefinition connector, final Object... suffixes) {
    final var idBuilder =
        new StringBuilder(
            String.format(
                "%s_%s_%s",
                SIP_CONNECTOR_PREFIX, connector.getId(), role.getRoleSuffixInRouteId()));
    Arrays.stream(suffixes).forEach(suffix -> idBuilder.append("-").append(suffix));
    final var routeId = idBuilder.toString();
    if (roleForRouteIdRegister.containsKey(routeId)) {
      throw SIPFrameworkInitializationException.init(
          "Internal SIP Error - Can't build internal connector route with ID '%s': Route already exists",
          routeId);
    }
    connectorForRouteIdRegister.put(routeId, connector);
    routeIdsForConnectorRegister.put(connector, routeId);
    roleForRouteIdRegister.put(routeId, role);
    return routeId;
  }

  @Synchronized
  public String generateRouteIdForSoapService(final String soapServiceName) {
    final var routeId = String.format("%s_%s", SIP_SOAP_SERVICE_PREFIX, soapServiceName);
    if (roleForRouteIdRegister.containsKey(routeId)) {
      throw SIPFrameworkInitializationException.init(
          "Internal SIP Error - Can't build internal soap-service route with ID '%s': Route already exists",
          routeId);
    }
    routeIdForSoapServiceRegister.put(routeId, soapServiceName);
    roleForRouteIdRegister.put(routeId, RouteRole.EXTERNAL_SOAP_SERVICE_PROXY);
    return routeId;
  }

  @Synchronized
  public String generateRouteIdForScenarioOrchestrator(
      final IntegrationScenarioDefinition scenario, final String... suffixes) {
    return generateRouteIdForOrchestration(
        scenario.getId(),
        RouteRole.SCENARIO_ORCHESTRATION,
        SIP_SCENARIO_ORCHESTRATOR_PREFIX,
        suffixes);
  }

  @Synchronized
  public String generateRouteIdForCompositeScenarioOrchestrator(
      final CompositeProcessDefinition process, final String suffix) {
    return generateRouteIdForOrchestration(
        process.getId(),
        RouteRole.COMPOSITE_SCENARIO_ORCHESTRATION,
        SIP_COMPOSITE_ORCHESTRATOR_PREFIX,
        suffix);
  }

  @Synchronized
  private String generateRouteIdForOrchestration(
      final String scenarioId, RouteRole routeRole, String prefix, String... suffixes) {
    final var idBuilder = new StringBuilder(prefix);
    idBuilder.append("_").append(scenarioId);
    Arrays.stream(suffixes).forEach(suffix -> idBuilder.append("_").append(suffix));
    final var routeId = idBuilder.toString();
    if (roleForRouteIdRegister.containsKey(routeId)) {
      throw SIPFrameworkInitializationException.init(
          "Internal SIP Error - Can't build internal orchestrator route with ID '%s': Route already exists",
          routeId);
    }
    roleForRouteIdRegister.put(routeId, routeRole);
    return routeId;
  }

  public RouteDeclarativeStructureInfo generateRouteInfo(String routeId) {
    ConnectorDefinition connectorDefinition = connectorForRouteIdRegister.get(routeId);
    if (connectorDefinition == null) {
      return null;
    }
    return RouteDeclarativeStructureInfo.builder()
        .connectorGroupId(connectorDefinition.getConnectorGroupId())
        .connectorId(connectorDefinition.getId())
        .scenarioId(connectorDefinition.getScenarioId())
        .build();
  }

  public String getRouteIdByConnectorIdAndRole(String connectorId, RouteRole role) {
    Optional<ConnectorDefinition> connector = declarationsRegistryApi.getConnectorById(connectorId);
    List<RouteInfo> routeInfos = new ArrayList<>();
    if (connector.isPresent()) {
      routeInfos = getRoutesInfo(connector.get());
    }
    return routeInfos.stream()
        .filter(routeInfo -> routeInfo.getRouteRole().equals(role.getExternalName()))
        .map(RouteInfo::getRouteId)
        .findFirst()
        .orElse(null);
  }

  public List<RouteInfo> getRoutesInfo(ConnectorDefinition connectorDefinition) {
    return routeIdsForConnectorRegister.get(connectorDefinition).stream()
        .map(
            routeId ->
                RouteInfo.builder()
                    .routeRole(roleForRouteIdRegister.get(routeId).getExternalName())
                    .routeId(routeId)
                    .build())
        .toList();
  }

  public String getConnectorIdByRouteId(String routeId) {
    ConnectorDefinition connector = connectorForRouteIdRegister.get(routeId);
    return connector != null ? connector.getId() : null;
  }

  public List<EndpointInfo> getExternalEndpointInfosForConnector(
      ConnectorDefinition connectorDefinition) {
    List<RouteInfo> connectorRoutes = getRoutesInfo(connectorDefinition);
    return connectorRoutes.stream()
        .map(
            routeInfo ->
                endpointsForRouteId.get(routeInfo.getRouteId()).stream()
                    // filter out all of sip framework internal endpoints
                    .filter(endpoint -> !endpoint.contains(SIP_CONNECTOR_PREFIX))
                    .filter(endpoint -> !isInMemoryUri(endpoint))
                    .map(
                        endpoint ->
                            createEndpointInfo(
                                endpoint,
                                routeInfo.getRouteId(),
                                isPrimaryEndpoint(
                                    connectorDefinition.getConnectorType(),
                                    routeInfo.getRouteRole())))
                    .toList())
        .flatMap(Collection::stream)
        .toList();
  }

  private EndpointInfo createEndpointInfo(String endpoint, String routeId, boolean isPrimary) {
    IdAware idAware = outgoingEndpointIds.get(endpoint);
    return EndpointInfo.builder()
        .endpointId(idAware != null ? idAware.getId() : routeId)
        .camelEndpointUri(endpoint)
        .primary(isPrimary)
        .build();
  }

  public List<RouteDeclarativeStructureInfo> generateRouteInfoList(Endpoint endpoint) {
    List<RouteDeclarativeStructureInfo> routeDeclarativeStructureInfoList = new ArrayList<>();
    routeIdsForEndpoints
        .get(endpoint.getEndpointBaseUri())
        .forEach(routeId -> routeDeclarativeStructureInfoList.add(generateRouteInfo(routeId)));
    return routeDeclarativeStructureInfoList;
  }

  void prefillEndpointRouteMappings(CamelContext camelContext) {
    initOutgoingEndpointIds(camelContext);
    initEndpointsAndRouteIdsMaps(camelContext);
  }

  private void initOutgoingEndpointIds(CamelContext camelContext) {
    ProcessorProxyRegistry proxiesRegistry =
        camelContext.getCamelContextExtension().getContextPlugin(ProcessorProxyRegistry.class);
    proxiesRegistry
        .getProxies()
        .values()
        .forEach(
            processorProxy -> {
              if (processorProxy.isEndpointProcessor()) {
                addProcessorId(processorProxy);
              }
            });
  }

  private void addProcessorId(ProcessorProxy processor) {
    Processor originalProcessor = processor.getOriginalProcessor();
    Optional<String> endpointUri = getEndpointUri(originalProcessor);
    endpointUri.ifPresent(uri -> outgoingEndpointIds.put(uri, (IdAware) originalProcessor));
  }

  private void initEndpointsAndRouteIdsMaps(CamelContext camelContext) {
    for (Route route : camelContext.getRoutes()) {
      String routeId = route.getRouteId();
      addToEndpointUriMaps(routeId, route.getEndpoint().getEndpointBaseUri());
      for (org.apache.camel.Service service : route.getServices()) {
        checkForExternalEndpoint(service, routeId);
      }
    }
  }

  private void checkForExternalEndpoint(org.apache.camel.Service service, String routeId) {
    // filter duplicated rest servlet endpoint
    if (service instanceof ServletConsumer) {
      return;
    }

    if (service instanceof Processor processorService) {
      getEndpointUri(processorService)
          .ifPresent(endpointUri -> addToEndpointUriMaps(routeId, endpointUri));
    }

    if (service instanceof Enricher enricher) {
      String enrichEndpointUri =
          getEnrichExpressionUri(enricher.getExpression(), ENRICH, enrichCounter);
      if (StringUtils.startsWith(enrichEndpointUri, ENRICH)) {
        enrichCounter++;
      }
      addToEndpointUriMaps(routeId, enrichEndpointUri);
      outgoingEndpointIds.put(enrichEndpointUri, enricher);
    }

    if (service instanceof PollEnricher pollEnricher) {
      String pollEnrichEndpointUri =
          getEnrichExpressionUri(pollEnricher.getExpression(), POLL_ENRICH, pollEnrichCounter);
      if (StringUtils.startsWith(pollEnrichEndpointUri, POLL_ENRICH)) {
        pollEnrichCounter++;
      }
      addToEndpointUriMaps(routeId, pollEnrichEndpointUri);
      outgoingEndpointIds.put(pollEnrichEndpointUri, pollEnricher);
    }
  }

  private void addToEndpointUriMaps(String routeId, String endpointUri) {
    endpointsForRouteId.put(routeId, endpointUri);
    routeIdsForEndpoints.put(endpointUri, routeId);
  }

  private String getEnrichExpressionUri(Expression expression, String processorName, int counter) {
    return expression != null
        ? expression.toString()
        : String.format("%s-%s", processorName, counter);
  }

  public void updateRouteId(String targetToBase, String routePath) {
    var connector = connectorForRouteIdRegister.remove(targetToBase);
    connectorForRouteIdRegister.put(routePath, connector);
    routeIdsForConnectorRegister.remove(connector);
    routeIdsForConnectorRegister.put(connector, routePath);
    var role = roleForRouteIdRegister.remove(targetToBase);
    roleForRouteIdRegister.put(routePath, role);
  }
}
