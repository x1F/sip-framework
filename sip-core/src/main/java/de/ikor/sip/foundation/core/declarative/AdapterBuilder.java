package de.ikor.sip.foundation.core.declarative;

import static de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper.appendOnException;
import static de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper.joinConfigurationIds;
import static de.ikor.sip.foundation.core.declarative.validator.CDMValidator.*;

import de.ikor.sip.foundation.core.declarative.annotation.connector.CleanupHeaders;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.connector.InboundConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.connector.OutboundConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioProviderDefinition;
import de.ikor.sip.foundation.core.declarative.validator.CDMValidator;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.builder.EndpointConsumerBuilder;
import org.apache.camel.builder.EndpointProducerBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.camel.builder.endpoint.dsl.DirectEndpointBuilderFactory;
import org.apache.camel.model.OptionalIdentifiedDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.model.rest.RestsDefinition;
import org.springframework.stereotype.Component;

/**
 * This class is setting up Camel routes from the scenario- and connector-definitions specified in
 * the adapter.
 *
 * <p><em>Intended for internal use only</em>
 */
@Component
@Slf4j
public final class AdapterBuilder extends RouteBuilder {

  private static final String PROCESS_HANDOFF_ROUTE_ID_PATTERN = "sip-process-handoff-%s";
  private static final String PROCESS_TAKEOVER_ROUTE_ID_PATTERN = "sip-process-takeover-%s";
  private final DeclarationsRegistry declarationsRegistry;
  private final RoutesRegistry routesRegistry;

  @SuppressWarnings("rawtypes")
  private final Map<IntegrationScenarioDefinition, List<InboundConnectorDefinition>>
      inboundConnectors;

  private final Map<IntegrationScenarioDefinition, List<OutboundConnectorDefinition>>
      outboundConnectors;

  @Override
  public void configure() {
    getCamelContext().getGlobalEndpointConfiguration().setBridgeErrorHandler(true);
    declarationsRegistry.getScenarios().forEach(this::buildScenario);
    declarationsRegistry.getProcesses().forEach(this::buildCompositeProcess);
  }

  public AdapterBuilder(DeclarationsRegistry declarationsRegistry, RoutesRegistry routesRegistry) {
    this.declarationsRegistry = declarationsRegistry;
    this.routesRegistry = routesRegistry;
    this.inboundConnectors =
        declarationsRegistry.getInboundConnectors().stream()
            .collect(
                Collectors.groupingBy(
                    connectors ->
                        declarationsRegistry.getScenarioById(connectors.getScenarioId())));
    this.outboundConnectors =
        declarationsRegistry.getOutboundConnectors().stream()
            .collect(
                Collectors.groupingBy(
                    connectors ->
                        declarationsRegistry.getScenarioById(connectors.getScenarioId())));
  }

  @SuppressWarnings("unchecked")
  private void buildScenario(final IntegrationScenarioDefinition scenarioDefinition) {

    final Map<
            IntegrationScenarioProviderDefinition,
            DirectEndpointBuilderFactory.DirectEndpointBuilder>
        providerHandoffEndpoints = new HashMap<>();

    if (inboundConnectors.get(scenarioDefinition) != null) {
      for (final var provider : inboundConnectors.get(scenarioDefinition)) {
        final var endpoint =
            StaticEndpointBuilders.direct(
                String.format("sip-scenario-handoff-%s", provider.getId()));
        providerHandoffEndpoints.put(provider, endpoint);
        buildInboundConnector(provider, scenarioDefinition, endpoint);
      }
    }

    declarationsRegistry
        .getCompositeProcessProvidersForScenario(scenarioDefinition)
        .forEach(
            composite -> {
              final var endpoint =
                  StaticEndpointBuilders.direct(
                      String.format(
                          PROCESS_HANDOFF_ROUTE_ID_PATTERN,
                          composite.getId() + "-" + scenarioDefinition.getId()));
              providerHandoffEndpoints.put(composite, endpoint);
            });

    final Map<
            IntegrationScenarioConsumerDefinition,
            DirectEndpointBuilderFactory.DirectEndpointBuilder>
        consumerTakeoverEndpoints = new HashMap<>();

    if (outboundConnectors.get(scenarioDefinition) != null) {
      for (final var consumer : outboundConnectors.get(scenarioDefinition)) {
        final var endpoint =
            StaticEndpointBuilders.direct(
                String.format("sip-scenario-takeover-%s", consumer.getId()));
        consumerTakeoverEndpoints.put(consumer, endpoint);
        buildOutboundConnector(consumer, scenarioDefinition, endpoint);
      }
    }

    declarationsRegistry
        .getCompositeProcessConsumersForScenario(scenarioDefinition)
        .forEach(
            composite -> {
              final var endpoint =
                  StaticEndpointBuilders.direct(
                      String.format(
                          PROCESS_TAKEOVER_ROUTE_ID_PATTERN,
                          composite.getId() + "-" + scenarioDefinition.getId()));
              consumerTakeoverEndpoints.put(composite, endpoint);
            });

    final var orchestrationInfo =
        new ScenarioOrchestrationValues(
            scenarioDefinition,
            getRouteCollection(),
            Collections.unmodifiableMap(providerHandoffEndpoints),
            Collections.unmodifiableMap(consumerTakeoverEndpoints));
    if (!scenarioDefinition.getOrchestrator().canOrchestrate(orchestrationInfo)) {
      throw SIPFrameworkInitializationException.init(
          "Orchestrator assigned to scenario '%s' declares being unable to orchestrate the scenario layout as it is defined",
          scenarioDefinition.getId());
    }
    scenarioDefinition.getOrchestrator().doOrchestrate(orchestrationInfo);
  }

  private <T extends OptionalIdentifiedDefinition<T>> void buildInboundConnector(
      final InboundConnectorDefinition<T> inboundConnector,
      final IntegrationScenarioDefinition scenarioDefinition,
      final EndpointProducerBuilder handoffToEndpoint) {

    final var requestOrchestrationRouteId =
        routesRegistry.generateRouteIdForConnector(
            RouteRole.CONNECTOR_REQUEST_ORCHESTRATION, inboundConnector);
    final var responseOrchestrationRouteId =
        routesRegistry.generateRouteIdForConnector(
            RouteRole.CONNECTOR_RESPONSE_ORCHESTRATION, inboundConnector);
    final var scenarioHandoffRouteId =
        routesRegistry.generateRouteIdForConnector(RouteRole.SCENARIO_HANDOFF, inboundConnector);

    final var headerCleanupProcessor =
        Optional.ofNullable(inboundConnector.getClass().getAnnotation(CleanupHeaders.class))
            .map(annotation -> new HeaderCleanupProcessors(annotation.keep(), inboundConnector));

    // Channel all inbound camel endpoint routes to the orchestration route
    final var endpointDefinitionType = inboundConnector.getEndpointDefinitionTypeClass();
    List<String> directRoutes =
        inboundConnector.defineInboundEndpoints(
            resolveConnectorDefinitionType(endpointDefinitionType),
            requestOrchestrationRouteId,
            routesRegistry,
            declarationsRegistry);

    // Build scenario handoff and response-route
    String routeConfigurationIds =
        joinConfigurationIds(
            inboundConnector.getId(),
            inboundConnector.getConfigurationIds(),
            scenarioDefinition.getConfigurationIds());
    final var handoffRouteDefinition =
        from(StaticEndpointBuilders.direct(scenarioHandoffRouteId))
            .routeId(scenarioHandoffRouteId)
            .routeConfigurationId(routeConfigurationIds);
    appendOnException(inboundConnector, handoffRouteDefinition, declarationsRegistry);
    headerCleanupProcessor.ifPresent(
        proc -> handoffRouteDefinition.process(proc::cleanHeadersProcessor));
    handoffRouteDefinition
        .process(
            new CDMValidator(
                scenarioDefinition.getId(),
                inboundConnector.getId(),
                scenarioDefinition.getRequestModelClass(),
                TO_CDM_EXCEPTION_MESSAGE))
        .to(handoffToEndpoint);
    scenarioDefinition
        .getResponseModelClass()
        .ifPresent(
            cdmModel ->
                handoffRouteDefinition.process(
                    new CDMValidator(
                        scenarioDefinition.getId(),
                        inboundConnector.getId(),
                        cdmModel,
                        FROM_CDM_EXCEPTION_MESSAGE)));
    headerCleanupProcessor.ifPresent(
        proc -> handoffRouteDefinition.process(proc::recreateHeadersProcessor));
    handoffRouteDefinition.to(StaticEndpointBuilders.direct(responseOrchestrationRouteId));

    // Build orchestration route(s) to/from scenario
    final RouteDefinition responseRouteDefinition =
        from(StaticEndpointBuilders.direct(responseOrchestrationRouteId))
            .routeId(responseOrchestrationRouteId)
            .routeConfigurationId(routeConfigurationIds);
    appendOnException(inboundConnector, responseRouteDefinition, declarationsRegistry);
    directRoutes.forEach(
        route -> {
          final var requestRouteDefinition =
              from(StaticEndpointBuilders.direct(route))
                  .routeId(route)
                  .routeConfigurationId(routeConfigurationIds);
          appendOnException(inboundConnector, requestRouteDefinition, declarationsRegistry);
          var orchestrationInfo =
              new OrchestrationRoutes(requestRouteDefinition, Optional.of(responseRouteDefinition));

          if (inboundConnector.getOrchestrator().canOrchestrate(orchestrationInfo)) {
            inboundConnector.getOrchestrator().doOrchestrate(orchestrationInfo);
          }
          requestRouteDefinition.to(StaticEndpointBuilders.direct(scenarioHandoffRouteId));
        });
  }

  private void buildOutboundConnector(
      final OutboundConnectorDefinition outboundConnector,
      final IntegrationScenarioDefinition scenarioDefinition,
      final EndpointConsumerBuilder takeoverFromEndpoint) {

    final var externalEndpointRouteId =
        routesRegistry.generateRouteIdForConnector(RouteRole.EXTERNAL_ENDPOINT, outboundConnector);
    final var requestOrchestrationRouteId =
        routesRegistry.generateRouteIdForConnector(
            RouteRole.CONNECTOR_REQUEST_ORCHESTRATION, outboundConnector);
    final var responseOrchestrationRouteId =
        routesRegistry.generateRouteIdForConnector(
            RouteRole.CONNECTOR_RESPONSE_ORCHESTRATION, outboundConnector);
    final var scenarioTakeoverRouteId =
        routesRegistry.generateRouteIdForConnector(RouteRole.SCENARIO_TAKEOVER, outboundConnector);

    final var headerCleanupProcessor =
        Optional.ofNullable(outboundConnector.getClass().getAnnotation(CleanupHeaders.class))
            .map(annotation -> new HeaderCleanupProcessors(annotation.keep(), outboundConnector));

    String configIds =
        joinConfigurationIds(
            outboundConnector.getId(),
            outboundConnector.getConfigurationIds(),
            scenarioDefinition.getConfigurationIds());
    // Build takeover route from scenario
    RouteDefinition routeDefinition =
        from(takeoverFromEndpoint).routeId(scenarioTakeoverRouteId).routeConfigurationId(configIds);
    appendOnException(outboundConnector, routeDefinition, declarationsRegistry);
    routeDefinition
        .process(
            new CDMValidator(
                scenarioDefinition.getId(),
                outboundConnector.getId(),
                scenarioDefinition.getRequestModelClass(),
                FROM_CDM_EXCEPTION_MESSAGE))
        .to(StaticEndpointBuilders.direct(requestOrchestrationRouteId));

    // Build endpoint route that connects to external system
    final var endpointRouteDefinition =
        from(StaticEndpointBuilders.direct(externalEndpointRouteId))
            .routeId(externalEndpointRouteId)
            .routeConfigurationId(configIds);
    appendOnException(outboundConnector, endpointRouteDefinition, declarationsRegistry);
    headerCleanupProcessor.ifPresent(
        proc -> endpointRouteDefinition.process(proc::cleanHeadersProcessor));
    outboundConnector.defineOutboundEndpoints(endpointRouteDefinition);
    headerCleanupProcessor.ifPresent(
        proc -> endpointRouteDefinition.process(proc::recreateHeadersProcessor));
    endpointRouteDefinition.to(StaticEndpointBuilders.direct(responseOrchestrationRouteId));

    // Build orchestration route(s) to/from scenario
    final var requestRouteDefinition =
        from(StaticEndpointBuilders.direct(requestOrchestrationRouteId))
            .routeId(requestOrchestrationRouteId)
            .routeConfigurationId(configIds);
    appendOnException(outboundConnector, requestRouteDefinition, declarationsRegistry);
    final RouteDefinition responseRouteDefinition =
        from(StaticEndpointBuilders.direct(responseOrchestrationRouteId))
            .routeId(responseOrchestrationRouteId)
            .routeConfigurationId(configIds);
    appendOnException(outboundConnector, responseRouteDefinition, declarationsRegistry);

    var orchestrationInfo =
        new OrchestrationRoutes(requestRouteDefinition, Optional.of(responseRouteDefinition));
    if (outboundConnector.getOrchestrator().canOrchestrate(orchestrationInfo)) {
      outboundConnector.getOrchestrator().doOrchestrate(orchestrationInfo);
    }
    requestRouteDefinition.to(StaticEndpointBuilders.direct(externalEndpointRouteId));

    scenarioDefinition
        .getResponseModelClass()
        .ifPresent(
            cdmModel ->
                responseRouteDefinition.process(
                    new CDMValidator(
                        scenarioDefinition.getId(),
                        outboundConnector.getId(),
                        cdmModel,
                        TO_CDM_EXCEPTION_MESSAGE)));
  }

  @SuppressWarnings("unchecked")
  private <T extends OptionalIdentifiedDefinition<T>> T resolveConnectorDefinitionType(
      Class<? extends T> type) {
    if (type.equals(RoutesDefinition.class)) {
      var routeCollection = getRouteCollection();
      routeCollection.setCamelContext(getCamelContext());
      return (T) routeCollection;
    }
    if (type.equals(RestsDefinition.class)) {
      var restCollection = getRestCollection();
      restCollection.setCamelContext(getCamelContext());
      return (T) restCollection;
    }
    throw SIPFrameworkInitializationException.init(
        "Failed to resolve unknown connector definition type: %s", type.getName());
  }

  private void buildCompositeProcess(CompositeProcessDefinition compositeProcess) {

    final Map<IntegrationScenarioDefinition, DirectEndpointBuilderFactory.DirectEndpointBuilder>
        providerHandoffEndpoints = new HashMap<>();
    final Map<IntegrationScenarioDefinition, DirectEndpointBuilderFactory.DirectEndpointBuilder>
        consumerTakeoverEndpoints = new HashMap<>();

    IntegrationScenarioDefinition providerScenario =
        declarationsRegistry.getScenarios().stream()
            .filter(s -> s.getClass().equals(compositeProcess.getProviderDefinition()))
            .findFirst()
            .orElseThrow(
                () ->
                    SIPFrameworkInitializationException.init(
                        "Composite process '%s' uses a provider scenario '%s' which couldn't be found in the registry. Please check your configuration.",
                        compositeProcess.getId(),
                        compositeProcess.getProviderDefinition().getName()));
    final var startingEndpoint =
        StaticEndpointBuilders.direct(
            String.format(
                PROCESS_TAKEOVER_ROUTE_ID_PATTERN,
                compositeProcess.getId() + "-" + providerScenario.getId()));
    providerHandoffEndpoints.put(providerScenario, startingEndpoint);

    compositeProcess
        .getConsumerDefinitions()
        .forEach(
            consumer -> {
              IntegrationScenarioDefinition consumerScenario =
                  declarationsRegistry.getScenarios().stream()
                      .filter(s -> s.getClass().equals(consumer))
                      .findFirst()
                      .orElseThrow(
                          () ->
                              SIPFrameworkInitializationException.init(
                                  "Composite process '%s' uses a consumer scenario '%s' which couldn't be found in the registry. Please check your configuration.",
                                  compositeProcess.getId(), consumer.getName()));
              var endingEndpoint =
                  StaticEndpointBuilders.direct(
                      String.format(
                          PROCESS_HANDOFF_ROUTE_ID_PATTERN,
                          compositeProcess.getId() + "-" + consumerScenario.getId()));
              consumerTakeoverEndpoints.put(consumerScenario, endingEndpoint);
            });

    final var orchestrationInfo =
        new CompositeProcessOrchestrationValues(
            compositeProcess,
            getRouteCollection(),
            Collections.unmodifiableMap(providerHandoffEndpoints),
            Collections.unmodifiableMap(consumerTakeoverEndpoints));
    if (!compositeProcess.getOrchestrator().canOrchestrate(orchestrationInfo)) {
      throw SIPFrameworkInitializationException.init(
          "Orchestrator assigned to composite process '%s' declares being unable to orchestrate the orchestration layout as it is defined",
          compositeProcess.getId());
    }
    compositeProcess.getOrchestrator().doOrchestrate(orchestrationInfo);
  }

  @Value
  private static class OrchestrationRoutes implements ConnectorOrchestrationInfo {

    RouteDefinition requestRouteDefinition;
    Optional<RouteDefinition> responseRouteDefinition;
  }

  @Value
  private static class ScenarioOrchestrationValues implements ScenarioOrchestrationInfo {

    IntegrationScenarioDefinition integrationScenario;
    RoutesDefinition routesDefinition;
    Map<IntegrationScenarioProviderDefinition, EndpointConsumerBuilder> providerEndpoints;
    Map<IntegrationScenarioConsumerDefinition, EndpointProducerBuilder> consumerEndpoints;
  }

  @Value
  private static class CompositeProcessOrchestrationValues
      implements CompositeProcessOrchestrationInfo {

    CompositeProcessDefinition compositeProcess;
    RoutesDefinition routesDefinition;
    Map<IntegrationScenarioDefinition, EndpointConsumerBuilder> providerEndpoints;
    Map<IntegrationScenarioDefinition, EndpointProducerBuilder> consumerEndpoints;
  }

  /**
   * Class that cleans and reinstates headers for {@link OutboundConnectorDefinition}s that are
   * annotated with {@link CleanupHeaders}. <em>For internal use only</em>
   *
   * @see CleanupHeaders
   */
  static class HeaderCleanupProcessors {

    private static final String HEADER_MEMORY_PROPERTY_NAME = "sip-header-cleanup-memory-%s";
    private final List<Pattern> keepHeaders;
    private final String memoryPropertyName;

    HeaderCleanupProcessors(final String[] keepHeaders, final ConnectorDefinition connector) {
      this(
          Arrays.stream(keepHeaders)
              .map(pattern -> Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
              .toArray(Pattern[]::new),
          connector);
    }

    HeaderCleanupProcessors(final Pattern[] keepHeaders, final ConnectorDefinition connector) {
      this.keepHeaders = List.of(keepHeaders);
      this.memoryPropertyName = String.format(HEADER_MEMORY_PROPERTY_NAME, connector.getId());
    }

    void cleanHeadersProcessor(final Exchange exchange) {
      final var message = exchange.getMessage();
      final var headers = message.getHeaders();
      final var toRemove =
          headers.keySet().stream()
              .filter(
                  headerName ->
                      keepHeaders.stream()
                          .noneMatch(pattern -> pattern.matcher(headerName).matches()))
              .toList();
      if (!toRemove.isEmpty()) {
        final var copy = new HashMap<>(headers);
        copy.keySet().retainAll(toRemove);
        toRemove.forEach(message::removeHeader);
        exchange.setProperty(memoryPropertyName, copy);
      }
    }

    @SuppressWarnings("unchecked")
    void recreateHeadersProcessor(final Exchange exchange) {
      final Map<String, Object> storedHeaders = exchange.getProperty(memoryPropertyName, Map.class);
      if (null != storedHeaders) {
        final var message = exchange.getMessage();
        for (var entry : storedHeaders.entrySet()) {
          if (null == message.getHeader(entry.getKey())) {
            message.setHeader(entry.getKey(), entry.getValue());
          }
        }
        exchange.removeProperty(memoryPropertyName);
      }
    }
  }
}
