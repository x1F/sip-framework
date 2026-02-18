package one.x1f.sip.foundation.core.declarative.orchestration.connector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.RoutesRegistry;
import one.x1f.sip.foundation.core.declarative.annotation.UseRequestModelMapper;
import one.x1f.sip.foundation.core.declarative.annotation.UseResponseModelMapper;
import one.x1f.sip.foundation.core.declarative.annotation.connector.extension.*;
import one.x1f.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import one.x1f.sip.foundation.core.declarative.connector.*;
import one.x1f.sip.foundation.core.declarative.model.ModelMapper;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestrator;
import one.x1f.sip.foundation.core.declarative.utils.DeclarativeHelper;
import one.x1f.sip.foundation.core.util.StreamHelper;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.builder.endpoint.StaticEndpointBuilders;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public final class ConnectorExtensionChainOrchestrator
    implements Orchestrator<ConnectorOrchestrationInfo> {

  public static final String EXTENSION_ID_REQUEST = "%s-extension-request-%s";
  public static final String EXTENSION_ID_RESPONSE = "%s-extension-response-%s";

  final Supplier<ConnectorDefinition> relatedConnector;
  final Supplier<ApplicationContext> applicationContext;

  @Getter
  final Map<String, ConnectorExtensionRegistryEntry> requestExtensionsRegistry = new HashMap<>();

  @Getter
  final Map<String, ConnectorExtensionRegistryEntry> responseExtensionsRegistry = new HashMap<>();

  @Override
  public boolean canOrchestrate(ConnectorOrchestrationInfo info) {
    return info != null;
  }

  @Override
  public void doOrchestrate(ConnectorOrchestrationInfo info) {
    final var connector = relatedConnector.get();
    final var context = applicationContext.get();

    RoutesRegistry routesRegistry = context.getBean(RoutesRegistry.class);

    buildExtensionRegistryForConnector(connector, context);

    final var orderedRequestExtensions =
        orderExtensionsByAnnotations(
            Collections.unmodifiableCollection(requestExtensionsRegistry.values()));
    logListOrder(
        String.format("Order of request-extensions for connector %s: ", connector.getId()),
        orderedRequestExtensions);
    if (!orderedRequestExtensions.isEmpty()) {
      var requestRoute = info.getRequestRouteDefinition();
      for (var extension : orderedRequestExtensions) {
        String extensionId =
            String.format(EXTENSION_ID_REQUEST, connector.getId(), extension.getExtensionName());
        routesRegistry.addProcessorExtension(connector.getId(), extensionId);
        requestRoute.to(StaticEndpointBuilders.direct(extensionId));
      }
    }

    if (info.getResponseRouteDefinition().isPresent()) {
      var responseRoute = info.getResponseRouteDefinition().orElseThrow();
      if (responseExtensionsRegistry.isEmpty()) {
        responseRoute.process(exchange -> {});
      } else {
        var orderedResponseExtensions =
            orderExtensionsByAnnotations(
                Collections.unmodifiableCollection(responseExtensionsRegistry.values()));
        logListOrder(
            String.format("Order of response-extensions for connector %s: ", connector.getId()),
            orderedResponseExtensions);
        for (var extension : orderedResponseExtensions) {
          String extensionId =
              String.format(EXTENSION_ID_RESPONSE, connector.getId(), extension.getExtensionName());
          routesRegistry.addProcessorExtension(connector.getId(), extensionId);
          responseRoute.to(StaticEndpointBuilders.direct(extensionId));
        }
      }
    }
  }

  private static void logListOrder(
      String prefix, List<ConnectorExtension> orderedRequestExtensions) {
    if (!orderedRequestExtensions.isEmpty()) {
      log.info(
          "{} {}",
          prefix,
          orderedRequestExtensions.stream()
              .map(ConnectorExtension::getExtensionName)
              .collect(Collectors.joining(" => ")));
    }
  }

  private List<ConnectorExtension> orderExtensionsByAnnotations(
      final Collection<ConnectorExtensionRegistryEntry> unorderedImmutable) {

    final var originalExtensionSize = unorderedImmutable.size();
    final var unordered = new ArrayList<>(unorderedImmutable);
    final List<ConnectorExtension> orderedExtensions = new ArrayList<>(originalExtensionSize);
    final List<ConnectorExtensionRegistryEntry> absoluteOrderedEntries = new ArrayList<>();
    final List<ConnectorExtensionRegistryEntry> relativeOrderedEntries = new LinkedList<>();
    final List<ConnectorExtensionRegistryEntry> unorderedEntries = new ArrayList<>();
    final Optional<ConnectorExtensionRegistryEntry> firstEntry =
        StreamHelper.findAtMostOne(
            unordered.stream(),
            ConnectorExtensionRegistryEntry::isPlacedFirst,
            () ->
                SIPFrameworkInitializationException.init(
                    "More than one connector extension is ordered as first via @%s for connector %s",
                    ExecutionOrder.class.getSimpleName(), relatedConnector.get().getClass()));

    final Optional<ConnectorExtensionRegistryEntry> lastEntry =
        StreamHelper.findAtMostOne(
            unordered.stream(),
            ConnectorExtensionRegistryEntry::isPlacedLast,
            () ->
                SIPFrameworkInitializationException.init(
                    "More than one connector extension is ordered as last via @%s for connector %s",
                    ExecutionOrder.class.getSimpleName(), relatedConnector.get().getClass()));

    // sort unordered elements into buckets
    firstEntry.ifPresent(unordered::remove);
    lastEntry.ifPresent(unordered::remove);

    for (var entry : unordered) {
      if (entry.getPlacementBeforeExtension().isPresent()
          || entry.getPlacementAfterExtension().isPresent()) {
        relativeOrderedEntries.add(entry);
      } else if (entry.getPlacementAbsolute().isPresent()) {
        absoluteOrderedEntries.add(entry);
      } else {
        unorderedEntries.add(entry);
      }
    }

    // first, sort the list with entries that have absolute ordering
    absoluteOrderedEntries.sort(Comparator.comparing(o -> o.getPlacementAbsolute().orElseThrow()));
    orderedExtensions.addAll(
        absoluteOrderedEntries.stream()
            .map(ConnectorExtensionRegistryEntry::getExtension)
            .toList());

    // second, add all unordered elements to the end, so they can still be referred to by relative
    // orderings
    orderedExtensions.addAll(
        unorderedEntries.stream().map(ConnectorExtensionRegistryEntry::getExtension).toList());

    // place elements with a relative placement on elements inside the ordered list accordingly
    var placeableRelations =
        relativeOrderedEntries.stream()
            .filter(e -> hasExtensionRelativeRelationToList(e, orderedExtensions))
            .toList();
    while (!placeableRelations.isEmpty()) {
      relativeOrderedEntries.removeAll(placeableRelations);
      placeableRelations.forEach(e -> placeRelativeOrderedExtensionInList(e, orderedExtensions));
      placeableRelations =
          relativeOrderedEntries.stream()
              .filter(e -> hasExtensionRelativeRelationToList(e, orderedExtensions))
              .toList();
    }

    // sort and attach any remaining items to the end of the ordered list
    relativeOrderedEntries.sort(ConnectorExtensionChainOrchestrator::compareRelativeOrderedEntries);
    orderedExtensions.addAll(
        relativeOrderedEntries.stream()
            .map(ConnectorExtensionRegistryEntry::getExtension)
            .toList());

    // Add first and last entries last, to make absolutely sure the list does not shift any more
    firstEntry.ifPresent(entry -> orderedExtensions.add(0, entry.getExtension()));
    lastEntry.ifPresent(entry -> orderedExtensions.add(entry.getExtension()));

    // validate
    SIPFrameworkInitializationException.throwIf(
        orderedExtensions.size() != originalExtensionSize,
        "Number of ordered connector-extensions (%d) differs from expected amount of given registry-entries (%d)",
        orderedExtensions.size(),
        originalExtensionSize);

    return orderedExtensions;
  }

  private void placeRelativeOrderedExtensionInList(
      final ConnectorExtensionRegistryEntry entry, final List<ConnectorExtension> orderedList) {
    Optional<ConnectorExtension> placementBeforeExtension = entry.getPlacementBeforeExtension();
    if (placementBeforeExtension.isPresent()) {
      var indexBefore = orderedList.indexOf(placementBeforeExtension.get());
      if (indexBefore > -1) {
        orderedList.add(indexBefore, entry.getExtension());
        return;
      }
    }
    Optional<ConnectorExtension> placementAfterExtension = entry.getPlacementAfterExtension();
    if (placementAfterExtension.isPresent()) {
      var indexBefore = orderedList.indexOf(placementAfterExtension.get());
      if (indexBefore > -1) {
        orderedList.add(indexBefore + 1, entry.getExtension());
        return;
      }
    }
    throw SIPFrameworkInitializationException.init(
        "Failed to find correct relative placement position for connector extension '%s'",
        entry.getExtension().getExtensionName());
  }

  private boolean hasExtensionRelativeRelationToList(
      ConnectorExtensionRegistryEntry entry, List<ConnectorExtension> orderedList) {
    Set<ConnectorExtension> lookup = new HashSet<>();
    entry.getPlacementAfterExtension().ifPresent(lookup::add);
    entry.getPlacementBeforeExtension().ifPresent(lookup::add);
    return orderedList.stream().anyMatch(lookup::contains);
  }

  private static int compareRelativeOrderedEntries(
      final ConnectorExtensionRegistryEntry first, final ConnectorExtensionRegistryEntry second) {

    var firstExt = first.getExtension();
    var secondExt = second.getExtension();
    var firstBefore = first.getPlacementBeforeExtension();
    var firstAfter = first.getPlacementAfterExtension();
    var secondBefore = second.getPlacementBeforeExtension();
    var secondAfter = second.getPlacementAfterExtension();

    firstBefore.ifPresent(
        f ->
            secondBefore.ifPresent(
                s ->
                    SIPFrameworkInitializationException.throwIf(
                        f.equals(secondExt) && s.equals(firstExt),
                        "Unsatisfiable placement: connector-extension '%s' demands placement before '%s', and vice versa",
                        firstExt.getExtensionName(),
                        secondExt.getExtensionName())));

    firstAfter.ifPresent(
        f ->
            secondAfter.ifPresent(
                s ->
                    SIPFrameworkInitializationException.throwIf(
                        f.equals(secondExt) && s.equals(firstExt),
                        "Unsatisfiable placement: connector-extension '%s' demands placement after '%s', and vice versa",
                        firstExt.getExtensionName(),
                        secondExt.getExtensionName())));

    if (firstBefore.isPresent() && firstBefore.get().equals(secondExt)) {
      return -1;
    }

    if (firstAfter.isPresent() && firstAfter.get().equals(secondExt)) {
      return 1;
    }

    if (secondBefore.isPresent() && secondBefore.get().equals(firstExt)) {
      return -1;
    }

    if (secondAfter.isPresent() && secondAfter.get().equals(firstExt)) {
      return 1;
    }

    return 0;
  }

  private void buildExtensionRegistryForConnector(
      final ConnectorDefinition connector, final ApplicationContext context) {

    // Find and register all method-based connector processors
    registerMethodBasedProcessors(connector);

    // Find and register bean-based processors
    registerBeanBasedProcessors(connector, context);

    // Find and register method-based connector extensions
    registerMethodBasedExtensions(connector);

    // Find and register bean-based extensions
    registerBeanBasedExtensions(connector, context);

    // Register attached mappers
    registerMapperProcessor(
        context,
        connector,
        requestExtensionsRegistry,
        UseRequestModelMapper.class,
        UseRequestModelMapper::value);
    registerMapperProcessor(
        context,
        connector,
        responseExtensionsRegistry,
        UseResponseModelMapper.class,
        UseResponseModelMapper::value);
  }

  private <T extends Annotation> void registerMapperProcessor(
      ApplicationContext context,
      final ConnectorDefinition connector,
      final Map<String, ConnectorExtensionRegistryEntry> registry,
      final Class<T> annotationClass,
      final Function<T, Class<? extends ModelMapper>> mapperFetcher) {
    if (connector.getClass().isAnnotationPresent(annotationClass)) {
      final var annotation = connector.getClass().getAnnotation(annotationClass);
      final var mapper =
          DeclarativeHelper.createMapperInstance(context, mapperFetcher.apply(annotation));
      final var entry =
          new ConnectorExtensionRegistryEntry(
              mapper, connector.getClass(), requestExtensionsRegistry);
      registry.put(mapper.getExtensionName(), entry);
    }
  }

  private void registerBeanBasedProcessors(
      final ConnectorDefinition connector, final ApplicationContext context) {
    context.getBeansWithAnnotation(RequestProcessor.class).values().stream()
        .filter(
            bean ->
                isExtensionBeanForThisConnector(
                    connector,
                    bean,
                    RequestProcessor.class,
                    RequestProcessor::value,
                    RequestProcessor::connectorId))
        .flatMap(StreamHelper.typeFilter(ConnectorProcessor.class))
        .forEach(bean -> registerBeanBasedExtension(bean, requestExtensionsRegistry));
    context.getBeansWithAnnotation(ResponseProcessor.class).values().stream()
        .filter(
            bean ->
                isExtensionBeanForThisConnector(
                    connector,
                    bean,
                    ResponseProcessor.class,
                    ResponseProcessor::value,
                    ResponseProcessor::connectorId))
        .flatMap(StreamHelper.typeFilter(ConnectorProcessor.class))
        .forEach(bean -> registerBeanBasedExtension(bean, responseExtensionsRegistry));
  }

  private void registerBeanBasedExtensions(
      final ConnectorDefinition connector, final ApplicationContext context) {
    context.getBeansWithAnnotation(RequestExtension.class).values().stream()
        .filter(
            bean ->
                isExtensionBeanForThisConnector(
                    connector,
                    bean,
                    RequestExtension.class,
                    RequestExtension::value,
                    RequestExtension::connectorId))
        .flatMap(StreamHelper.typeFilter(ConnectorExtension.class))
        .forEach(bean -> registerBeanBasedExtension(bean, requestExtensionsRegistry));
    context.getBeansWithAnnotation(ResponseExtension.class).values().stream()
        .filter(
            bean ->
                isExtensionBeanForThisConnector(
                    connector,
                    bean,
                    ResponseExtension.class,
                    ResponseExtension::value,
                    ResponseExtension::connectorId))
        .flatMap(StreamHelper.typeFilter(ConnectorExtension.class))
        .forEach(bean -> registerBeanBasedExtension(bean, responseExtensionsRegistry));
  }

  private void registerMethodBasedProcessors(final ConnectorDefinition connector) {
    final var connectorMethods = connector.getClass().getMethods();
    Arrays.stream(connectorMethods)
        .filter(
            method ->
                method.isAnnotationPresent(RequestProcessor.class)
                    || method.isAnnotationPresent(ParameterMapping.class))
        .forEach(
            method -> registerMethodBasedProcessor(connector, method, requestExtensionsRegistry));
    Arrays.stream(connectorMethods)
        .filter(method -> method.isAnnotationPresent(ResponseProcessor.class))
        .forEach(
            method -> registerMethodBasedProcessor(connector, method, responseExtensionsRegistry));
  }

  private void registerMethodBasedExtensions(final ConnectorDefinition connector) {
    final var connectorMethods = connector.getClass().getMethods();
    Arrays.stream(connectorMethods)
        .filter(method -> method.isAnnotationPresent(RequestExtension.class))
        .forEach(
            method -> registerMethodBasedExtension(connector, method, requestExtensionsRegistry));
    Arrays.stream(connectorMethods)
        .filter(method -> method.isAnnotationPresent(ResponseExtension.class))
        .forEach(
            method -> registerMethodBasedExtension(connector, method, responseExtensionsRegistry));
  }

  @SneakyThrows
  private void registerMethodBasedExtension(
      ConnectorDefinition connector,
      Method method,
      Map<String, ConnectorExtensionRegistryEntry> registry) {
    final var extension =
        ConnectorExtension.class.isAssignableFrom(method.getReturnType())
            ? (ConnectorExtension) method.invoke(connector, null)
            : new MethodBasedConnectorExtension(connector, method);
    final var registryEntry = new ConnectorExtensionRegistryEntry(extension, method, registry);
    storeInRegistry(registryEntry, registry);
  }

  @SneakyThrows
  private void registerMethodBasedProcessor(
      final ConnectorDefinition connector,
      final Method method,
      final Map<String, ConnectorExtensionRegistryEntry> registry) {
    final var processor =
        ConnectorProcessor.class.isAssignableFrom(method.getReturnType())
            ? (ConnectorExtension) method.invoke(connector, null)
            : new MethodBasedConnectorProcessor(connector, method);
    final var registryEntry = new ConnectorExtensionRegistryEntry(processor, method, registry);
    storeInRegistry(registryEntry, registry);
  }

  private <T extends Annotation> boolean isExtensionBeanForThisConnector(
      final ConnectorDefinition targetConnector,
      final Object bean,
      final Class<T> annotationClass,
      final Function<T, Class<? extends ConnectorDefinition>> connectorClassFetcher,
      final Function<T, String> connectorIdFetcher) {
    if (bean instanceof ConnectorExtension extension) {
      final var annotation = extension.getClass().getAnnotation(annotationClass);
      final var connectorClass = connectorClassFetcher.apply(annotation);
      final var connectorId = connectorIdFetcher.apply(annotation);
      if (!ConnectorDefinition.None.class.equals(connectorClass)) {
        return targetConnector.getClass().equals(connectorClass);
      }
      if (Strings.isNotBlank(connectorId)) {
        return targetConnector.getId().equals(connectorId);
      }
      throw SIPFrameworkInitializationException.init(
          "ConnectorExtension in class %s does not declare any target connector in annotation @%s",
          bean.getClass(), annotationClass.getSimpleName());
    }
    return false;
  }

  private void registerBeanBasedExtension(
      final ConnectorExtension extension,
      final Map<String, ConnectorExtensionRegistryEntry> registry) {
    final var entry =
        new ConnectorExtensionRegistryEntry(extension, extension.getClass(), registry);
    storeInRegistry(entry, registry);
  }

  private void storeInRegistry(
      final ConnectorExtensionRegistryEntry registryEntry,
      final Map<String, ConnectorExtensionRegistryEntry> registry) {
    String extensionName = registryEntry.getExtension().getExtensionName();
    SIPFrameworkInitializationException.throwIf(
        registry.containsKey(extensionName),
        "A ConnectorExtension with name '%s' is used more than once for Connector '%s'",
        extensionName,
        relatedConnector.get().getId());
    registry.put(extensionName, registryEntry);
  }
}
