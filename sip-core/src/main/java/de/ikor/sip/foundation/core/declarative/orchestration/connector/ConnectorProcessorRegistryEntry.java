package de.ikor.sip.foundation.core.declarative.orchestration.connector;

import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteAfter;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteBefore;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteOrder;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import de.ikor.sip.foundation.core.util.StreamHelper;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import lombok.*;
import org.apache.logging.log4j.util.Strings;

@Value
@RequiredArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
class ConnectorProcessorRegistryEntry {

  @ToString.Include @EqualsAndHashCode.Include ConnectorProcessor processor;
  @EqualsAndHashCode.Include @ToString.Include AnnotatedElement definingElement;
  Map<String, ConnectorProcessorRegistryEntry> processorRegistry;

  @Getter(lazy = true)
  boolean placedFirst = resolvePlacedFirst();

  @Getter(lazy = true)
  boolean placedLast = resolvePlacedLast();

  @Getter(lazy = true)
  Optional<Integer> placementAbsolute = resolveAbsolutePosition();

  @Getter(lazy = true)
  Optional<ConnectorProcessor> placementBeforeProcessor =
      resolveRelativePlacedProcessor(
          ExecuteBefore.class, ExecuteBefore::value, ExecuteBefore::processorName);

  @Getter(lazy = true)
  Optional<ConnectorProcessor> placementAfterProcessor =
      resolveRelativePlacedProcessor(
          ExecuteAfter.class, ExecuteAfter::value, ExecuteAfter::processorName);

  @ToString.Include
  public String getProcessorName() {
    return processor.getProcessorName();
  }

  private boolean resolvePlacedFirst() {
    return DeclarativeReflectionUtils.getAnnotationIfPresent(ExecuteOrder.class, definingElement)
        .map(ExecuteOrder::first)
        .orElse(false);
  }

  private boolean resolvePlacedLast() {
    return DeclarativeReflectionUtils.getAnnotationIfPresent(ExecuteOrder.class, definingElement)
        .map(ExecuteOrder::last)
        .orElse(false);
  }

  private Optional<Integer> resolveAbsolutePosition() {
    final var annotation =
        DeclarativeReflectionUtils.getAnnotationIfPresent(ExecuteOrder.class, definingElement);
    if (annotation.isPresent() && annotation.get().value() > -1) {
      return annotation.map(ExecuteOrder::value);
    }
    return Optional.empty();
  }

  private <T extends Annotation> Optional<ConnectorProcessor> resolveRelativePlacedProcessor(
      final Class<T> annotationClass,
      Function<T, Class<? extends ConnectorProcessor>> procClassFetcher,
      Function<T, String> procNameFetcher) {
    final var annotation =
        DeclarativeReflectionUtils.getAnnotationIfPresent(annotationClass, definingElement);
    if (annotation.isPresent()) {
      // final var annotation = definingElement.getAnnotation(annotationClass);
      final Class<? extends ConnectorProcessor> relativeProcessorClass =
          procClassFetcher.apply(annotation.orElseThrow());
      final String relativeProcessorName = procNameFetcher.apply(annotation.orElseThrow());
      if (!ConnectorProcessor.None.class.equals(relativeProcessorClass)) {
        return Optional.of(findUniqueConnectorForClass(relativeProcessorClass));
      }
      if (Strings.isNotBlank(relativeProcessorName)) {
        final var element = processorRegistry.get(relativeProcessorName);
        SIPFrameworkInitializationException.throwIf(
            null == element,
            "No matching connector named '%s' could be found for relative placement defined in annotation %s in %s",
            relativeProcessorName,
            annotationClass.getSimpleName(),
            definingElement.getClass().getName());
        return Optional.of(element.getProcessor());
      }
      throw SIPFrameworkInitializationException.init(
          "No placement specified in annotation @%s in class %s",
          annotationClass.getSimpleName(), definingElement.getClass().getName());
    }
    return Optional.empty();
  }

  private ConnectorProcessor findUniqueConnectorForClass(
      final Class<? extends ConnectorProcessor> clazz) {

    final var matchingProcessor =
        StreamHelper.findAtMostOne(
            processorRegistry.values().stream().map(ConnectorProcessorRegistryEntry::getProcessor),
            proc -> clazz.equals(proc.getClass()),
            () ->
                SIPFrameworkInitializationException.init(
                    "More than one processor matched the relative placement restriction for processor-class '%s' in %s",
                    clazz.getName(), definingElement.getClass().getName()));

    matchingProcessor.ifPresent(
        match ->
            SIPFrameworkInitializationException.throwIf(
                processor.equals(match),
                "Relative placement for connector processor '%s' is pointing on itself",
                processor.getProcessorName()));

    return matchingProcessor.orElseThrow(
        () ->
            SIPFrameworkInitializationException.init(
                "No processor found that matched the relative placement restriction for processor-class '%s' in %s",
                clazz.getName(), definingElement.getClass().getName()));
  }
}
