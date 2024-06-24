package de.ikor.sip.foundation.core.declarative.orchestration.connector;

import de.ikor.sip.foundation.core.declarative.DeclarationsRegistry;
import de.ikor.sip.foundation.core.declarative.annonation.UseRequestModelMapper;
import de.ikor.sip.foundation.core.declarative.annonation.UseResponseModelMapper;
import de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorRequestProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorResponseProcessor;
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

    var requestRoute = info.getRequestRouteDefinition();
    for (var processorEntry : requestExtensionsRegistry.values()) {
      requestRoute = requestRoute.process(processorEntry.getProcessor());
    }

    if (info.getResponseRouteDefinition().isPresent()) {
      var responseRoute = info.getResponseRouteDefinition().get();
      if (responseExtensionsRegistry.isEmpty()) {
        responseRoute.process(exchange -> {});
      } else {
        for (var processorEntry : responseExtensionsRegistry.values()) {
          responseRoute = responseRoute.process(processorEntry.getProcessor());
        }
      }
    }
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
    context.getBeansWithAnnotation(ConnectorRequestProcessor.class).values().stream()
        .filter(
            bean ->
                isProcessorBeanForThisConnector(
                    connector,
                    bean,
                    ConnectorRequestProcessor.class,
                    ConnectorRequestProcessor::value,
                    ConnectorRequestProcessor::connectorId))
        .flatMap(StreamHelper.typeFilter(ConnectorProcessor.class))
        .forEach(bean -> registerBeanBasedProcessor(bean, requestExtensionsRegistry));
    context.getBeansWithAnnotation(ConnectorResponseProcessor.class).values().stream()
        .filter(
            bean ->
                isProcessorBeanForThisConnector(
                    connector,
                    bean,
                    ConnectorResponseProcessor.class,
                    ConnectorResponseProcessor::value,
                    ConnectorResponseProcessor::connectorId))
        .flatMap(StreamHelper.typeFilter(ConnectorProcessor.class))
        .forEach(bean -> registerBeanBasedProcessor(bean, responseExtensionsRegistry));
  }

  private void registerMethodBasedProcessors(final ConnectorDefinition connector) {
    final var connectorMethods = connector.getClass().getMethods();
    Arrays.stream(connectorMethods)
        .filter(
            method ->
                method.isAnnotationPresent(ConnectorRequestProcessor.class)
                    || method.isAnnotationPresent(ParameterMapping.class))
        .forEach(
            method -> registerMethodBasedProcessor(connector, method, requestExtensionsRegistry));
    Arrays.stream(connectorMethods)
        .filter(method -> method.isAnnotationPresent(ConnectorResponseProcessor.class))
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
            ? (ConnectorProcessor) method.invoke(method.getDeclaringClass(), null)
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
      if (!Strings.isNotBlank(connectorId)) {
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
    SIPFrameworkInitializationException.throwOn(
        registry.containsKey(processorName),
        "A ConnectorProcessor with name '%s' is used more than once for Connector '%s'",
        processorName,
        relatedConnector.get().getId());
    registry.put(processorName, registryEntry);
  }
}
