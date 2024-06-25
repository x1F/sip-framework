package de.ikor.sip.foundation.core.declarative.orchestration.connector;

import de.ikor.sip.foundation.core.declarative.DeclarationsRegistry;
import de.ikor.sip.foundation.core.declarative.annonation.UseRequestModelMapper;
import de.ikor.sip.foundation.core.declarative.annonation.UseResponseModelMapper;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteOrder;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.RequestProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ResponseProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.rest.ParameterMapping;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;
import de.ikor.sip.foundation.core.declarative.connector.MethodBasedConnectorProcessor;
import de.ikor.sip.foundation.core.declarative.model.ModelMapper;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper;
import de.ikor.sip.foundation.core.util.StreamHelper;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.ApplicationContext;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public final class ConnectorProcessorChainOrchestrator
    implements Orchestrator<ConnectorOrchestrationInfo> {

  public static final String PROCESSOR_ID_REQUEST = "%s-processor-request-%s";
  public static final String PROCESSOR_ID_RESPONSE = "%s-processor-response-%s";

  final Supplier<ConnectorDefinition> relatedConnector;
  final Supplier<ApplicationContext> applicationContext;
  final DeclarationsRegistry declarationsRegistry;
  final Map<String, ConnectorProcessorRegistryEntry> requestExtensionsRegistry = new HashMap<>();
  final Map<String, ConnectorProcessorRegistryEntry> responseExtensionsRegistry = new HashMap<>();

  @Override
  public boolean canOrchestrate(ConnectorOrchestrationInfo info) {
    return info != null;
  }

  @Override
  public void doOrchestrate(ConnectorOrchestrationInfo info) {
    final var connector = relatedConnector.get();
    final var context = applicationContext.get();

    buildProcessorRegistryForConnector(connector, context);

    final var orderedRequestProcessors =
        orderProcessorsByAnnotations(requestExtensionsRegistry.values());
    logListOrder(
        String.format("Order of request-processors for connector %s: ", connector.getId()),
        orderedRequestProcessors);
    var requestRoute = info.getRequestRouteDefinition();
    for (var processor : orderedRequestProcessors) {
      String processorId =
          String.format(PROCESSOR_ID_REQUEST, connector.getId(), processor.getProcessorName());
      requestRoute = requestRoute.process(processor).id(processorId);
    }

    if (info.getResponseRouteDefinition().isPresent()) {
      var responseRoute = info.getResponseRouteDefinition().get();
      if (responseExtensionsRegistry.isEmpty()) {
        responseRoute.process(exchange -> {});
      } else {
        var orderedResponseProcessors =
            orderProcessorsByAnnotations(responseExtensionsRegistry.values());
        logListOrder(
            String.format("Order of response-processors for connector %s: ", connector.getId()),
            orderedResponseProcessors);
        for (var processor : orderedResponseProcessors) {
          String processorId =
              String.format(PROCESSOR_ID_RESPONSE, connector.getId(), processor.getProcessorName());
          responseRoute = responseRoute.process(processor).id(processorId);
        }
      }
    }
  }

  private static void logListOrder(
      String prefix, List<ConnectorProcessor> orderedRequestProcessors) {
    if (!orderedRequestProcessors.isEmpty()) {
      log.info(
          "{} {}",
          prefix,
          orderedRequestProcessors.stream()
              .map(ConnectorProcessor::getProcessorName)
              .collect(Collectors.joining(" => ")));
    }
  }

  private List<ConnectorProcessor> orderProcessorsByAnnotations(
      final Collection<ConnectorProcessorRegistryEntry> unordered) {

    final var originalProcessorSize = unordered.size();
    final List<ConnectorProcessor> orderedProcessors = new ArrayList<>(unordered.size());
    final List<ConnectorProcessorRegistryEntry> absoluteOrderedEntries = new ArrayList<>();
    final List<ConnectorProcessorRegistryEntry> relativeOrderedEntries = new LinkedList<>();
    final List<ConnectorProcessorRegistryEntry> unorderedEntries = new ArrayList<>();
    final Optional<ConnectorProcessorRegistryEntry> firstEntry =
        StreamHelper.findAtMostOne(
            unordered.stream(),
            ConnectorProcessorRegistryEntry::isPlacedFirst,
            () ->
                SIPFrameworkInitializationException.init(
                    "More than one connector processor is ordered as first via @%s for connector %s",
                    ExecuteOrder.class.getSimpleName(), relatedConnector.get().getClass()));
    ;
    final Optional<ConnectorProcessorRegistryEntry> lastEntry =
        StreamHelper.findAtMostOne(
            unordered.stream(),
            ConnectorProcessorRegistryEntry::isPlacedLast,
            () ->
                SIPFrameworkInitializationException.init(
                    "More than one connector processor is ordered as last via @%s for connector %s",
                    ExecuteOrder.class.getSimpleName(), relatedConnector.get().getClass()));

    // sort unordered elements into buckets
    firstEntry.ifPresent(unordered::remove);
    lastEntry.ifPresent(unordered::remove);

    for (var entry : unordered) {
      if (entry.getPlacementBeforeProcessor().isPresent()
          || entry.getPlacementAfterProcessor().isPresent()) {
        relativeOrderedEntries.add(entry);
      } else if (entry.getPlacementAbsolute().isPresent()) {
        absoluteOrderedEntries.add(entry);
      } else {
        unorderedEntries.add(entry);
      }
    }

    // first, sort the list with entries that have absolute ordering
    absoluteOrderedEntries.sort(Comparator.comparing(o -> o.getPlacementAbsolute().orElseThrow()));
    orderedProcessors.addAll(
        absoluteOrderedEntries.stream()
            .map(ConnectorProcessorRegistryEntry::getProcessor)
            .toList());

    // second, add all unordered elements to the end, so they can still be referred to by relative
    // orderings
    orderedProcessors.addAll(
        unorderedEntries.stream().map(ConnectorProcessorRegistryEntry::getProcessor).toList());

    // place elements with a relative placement on elements inside the ordered list accordingly
    var placeableRelations =
        relativeOrderedEntries.stream()
            .filter(e -> hasProcessorRelativeRelationToList(e, orderedProcessors))
            .toList();
    while (!placeableRelations.isEmpty()) {
      relativeOrderedEntries.removeAll(placeableRelations);
      placeableRelations.forEach(e -> placeRelativeOrderedProcessorInList(e, orderedProcessors));
      placeableRelations =
          relativeOrderedEntries.stream()
              .filter(e -> hasProcessorRelativeRelationToList(e, orderedProcessors))
              .toList();
    }

    // sort and attach any remaining items to the end of the ordered list
    relativeOrderedEntries.sort(ConnectorProcessorChainOrchestrator::compareRelativeOrderedEntries);
    orderedProcessors.addAll(
        relativeOrderedEntries.stream()
            .map(ConnectorProcessorRegistryEntry::getProcessor)
            .toList());

    // Add first and last entries last, to make absolutely sure the list does not shift any more
    firstEntry.ifPresent(entry -> orderedProcessors.add(0, entry.getProcessor()));
    lastEntry.ifPresent(entry -> orderedProcessors.add(entry.getProcessor()));

    // validate
    SIPFrameworkInitializationException.throwIf(
        orderedProcessors.size() != originalProcessorSize,
        "Number of ordered connector-processors (%d) differs from expected amount of given registry-entries (%d)",
        orderedProcessors.size(),
        originalProcessorSize);

    return orderedProcessors;
  }

  private void placeRelativeOrderedProcessorInList(
      final ConnectorProcessorRegistryEntry entry, final List<ConnectorProcessor> orderedList) {
    if (entry.getPlacementBeforeProcessor().isPresent()) {
      var indexBefore = orderedList.indexOf(entry.getPlacementBeforeProcessor().get());
      if (indexBefore > -1) {
        orderedList.add(indexBefore, entry.getProcessor());
        return;
      }
    }
    if (entry.getPlacementAfterProcessor().isPresent()) {
      var indexBefore = orderedList.indexOf(entry.getPlacementAfterProcessor().get());
      if (indexBefore > -1) {
        orderedList.add(indexBefore + 1, entry.getProcessor());
        return;
      }
    }
    throw SIPFrameworkInitializationException.init(
        "Failed to find correct relative placement position for connector processor '%s'",
        entry.getProcessor().getProcessorName());
  }

  private boolean hasProcessorRelativeRelationToList(
      ConnectorProcessorRegistryEntry entry, List<ConnectorProcessor> orderedList) {
    Set<ConnectorProcessor> lookup = new HashSet<>();
    entry.getPlacementAfterProcessor().ifPresent(lookup::add);
    entry.getPlacementBeforeProcessor().ifPresent(lookup::add);
    return orderedList.stream().anyMatch(lookup::contains);
  }

  private static int compareRelativeOrderedEntries(
      final ConnectorProcessorRegistryEntry first, final ConnectorProcessorRegistryEntry second) {

    var firstProc = first.getProcessor();
    var secondProc = second.getProcessor();
    var firstBefore = first.getPlacementBeforeProcessor();
    var firstAfter = first.getPlacementAfterProcessor();
    var secondBefore = second.getPlacementBeforeProcessor();
    var secondAfter = second.getPlacementAfterProcessor();

    firstBefore.ifPresent(
        f ->
            secondBefore.ifPresent(
                s ->
                    SIPFrameworkInitializationException.throwIf(
                        f.equals(secondProc) && s.equals(firstProc),
                        "Unsatisfiable placement: connector-processor '%s' demands placement before '%s', and vice versa",
                        firstProc.getProcessorName(),
                        secondProc.getProcessorName())));

    firstAfter.ifPresent(
        f ->
            secondAfter.ifPresent(
                s ->
                    SIPFrameworkInitializationException.throwIf(
                        f.equals(secondProc) && s.equals(firstProc),
                        "Unsatisfiable placement: connector-processor '%s' demands placement after '%s', and vice versa",
                        firstProc.getProcessorName(),
                        secondProc.getProcessorName())));

    if (firstBefore.isPresent() && firstBefore.get().equals(secondProc)) {
      return -1;
    }

    if (firstAfter.isPresent() && firstAfter.get().equals(secondProc)) {
      return 1;
    }

    if (secondBefore.isPresent() && secondBefore.get().equals(firstProc)) {
      return -1;
    }

    if (secondAfter.isPresent() && secondAfter.get().equals(firstProc)) {
      return 1;
    }

    return 0;
  }

  private void buildProcessorRegistryForConnector(
      final ConnectorDefinition connector, final ApplicationContext context) {

    // Find and register all method-based connector processors
    registerMethodBasedProcessors(connector);

    // Find and register bean-based processors
    registerBeanBasedProcessors(connector, context);

    // Register attached mappers
    registerMapperProcessor(
        connector,
        requestExtensionsRegistry,
        UseRequestModelMapper.class,
        UseRequestModelMapper::value);
    registerMapperProcessor(
        connector,
        responseExtensionsRegistry,
        UseResponseModelMapper.class,
        UseResponseModelMapper::value);
  }

  private <T extends Annotation> void registerMapperProcessor(
      final ConnectorDefinition connector,
      final Map<String, ConnectorProcessorRegistryEntry> registry,
      final Class<T> annotationClass,
      final Function<T, Class<? extends ModelMapper>> mapperFetcher) {
    if (connector.getClass().isAnnotationPresent(annotationClass)) {
      final var annotation = connector.getClass().getAnnotation(annotationClass);
      final var mapper = DeclarativeHelper.createMapperInstance(mapperFetcher.apply(annotation));
      final var entry =
          new ConnectorProcessorRegistryEntry(
              mapper, connector.getClass(), requestExtensionsRegistry);
      registry.put(mapper.getProcessorName(), entry);
    }
  }

  private void registerBeanBasedProcessors(
      final ConnectorDefinition connector, final ApplicationContext context) {
    context.getBeansWithAnnotation(RequestProcessor.class).values().stream()
        .filter(
            bean ->
                isProcessorBeanForThisConnector(
                    connector,
                    bean,
                    RequestProcessor.class,
                    RequestProcessor::value,
                    RequestProcessor::connectorId))
        .flatMap(StreamHelper.typeFilter(ConnectorProcessor.class))
        .forEach(bean -> registerBeanBasedProcessor(bean, requestExtensionsRegistry));
    context.getBeansWithAnnotation(ResponseProcessor.class).values().stream()
        .filter(
            bean ->
                isProcessorBeanForThisConnector(
                    connector,
                    bean,
                    ResponseProcessor.class,
                    ResponseProcessor::value,
                    ResponseProcessor::connectorId))
        .flatMap(StreamHelper.typeFilter(ConnectorProcessor.class))
        .forEach(bean -> registerBeanBasedProcessor(bean, responseExtensionsRegistry));
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

  @SneakyThrows
  private void registerMethodBasedProcessor(
      final ConnectorDefinition connector,
      final Method method,
      final Map<String, ConnectorProcessorRegistryEntry> registry) {
    final var processor =
        ConnectorProcessor.class.isAssignableFrom(method.getReturnType())
            ? (ConnectorProcessor) method.invoke(connector, null)
            : new MethodBasedConnectorProcessor(connector, method);
    final var registryEntry = new ConnectorProcessorRegistryEntry(processor, method, registry);
    storeInRegistry(registryEntry, registry);
  }

  private <T extends Annotation> boolean isProcessorBeanForThisConnector(
      final ConnectorDefinition targetConnector,
      final Object bean,
      final Class<T> annotationClass,
      final Function<T, Class<? extends ConnectorDefinition>> connectorClassFetcher,
      final Function<T, String> connectorIdFetcher) {
    if (bean instanceof ConnectorProcessor processor) {
      final var annotation = processor.getClass().getAnnotation(annotationClass);
      final var connectorClass = connectorClassFetcher.apply(annotation);
      final var connectorId = connectorIdFetcher.apply(annotation);
      if (!ConnectorDefinition.None.class.equals(connectorClass)) {
        return targetConnector.getClass().equals(connectorClass);
      }
      if (Strings.isNotBlank(connectorId)) {
        return targetConnector.getId().equals(connectorId);
      }
      throw SIPFrameworkInitializationException.init(
          "Connector processor in class %s does not declare any target connector in annotation @%s",
          bean.getClass(), annotationClass.getSimpleName());
    }
    return false;
  }

  private void registerBeanBasedProcessor(
      final ConnectorProcessor processorBean,
      final Map<String, ConnectorProcessorRegistryEntry> registry) {
    final var entry =
        new ConnectorProcessorRegistryEntry(processorBean, processorBean.getClass(), registry);
    storeInRegistry(entry, registry);
  }

  private void storeInRegistry(
      final ConnectorProcessorRegistryEntry registryEntry,
      final Map<String, ConnectorProcessorRegistryEntry> registry) {
    String processorName = registryEntry.getProcessor().getProcessorName();
    SIPFrameworkInitializationException.throwIf(
        registry.containsKey(processorName),
        "A ConnectorProcessor with name '%s' is used more than once for Connector '%s'",
        processorName,
        relatedConnector.get().getId());
    registry.put(processorName, registryEntry);
  }
}
