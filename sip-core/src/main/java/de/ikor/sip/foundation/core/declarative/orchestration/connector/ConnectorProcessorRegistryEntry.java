package de.ikor.sip.foundation.core.declarative.orchestration.connector;

import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteOrder;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteAfter;
import de.ikor.sip.foundation.core.declarative.annotation.connector.processor.ExecuteBefore;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorProcessor;
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
class ConnectorProcessorRegistryEntry {

  ConnectorProcessor processor;
  AnnotatedElement definingElement;
  Map<String, ConnectorProcessorRegistryEntry> processorRegistry;

  @Getter(lazy = true)
  Optional<Integer> placementAbsolute = readAbsolutePosition();

  @Getter(lazy = true)
  Optional<ConnectorProcessor> placementBeforeProcessor =
      resolveRelativePlacedProcessor(
          ExecuteBefore.class,
          ExecuteBefore::value,
          ExecuteBefore::processorName);

  @Getter(lazy = true)
  Optional<ConnectorProcessor> placementAfterProcessor =
      resolveRelativePlacedProcessor(
          ExecuteAfter.class,
          ExecuteAfter::value,
          ExecuteAfter::processorName);

  private <T extends Annotation> Optional<ConnectorProcessor> resolveRelativePlacedProcessor(
      final Class<T> annotationClass,
      Function<T, Class<? extends ConnectorProcessor>> procClassFetcher,
      Function<T, String> procNameFetcher) {
    if (definingElement.isAnnotationPresent(annotationClass)) {
      final var annotation = definingElement.getAnnotation(annotationClass);
      final Class<? extends ConnectorProcessor> relativeProcessorClass =
          procClassFetcher.apply(annotation);
      final String relativeProcessorName = procNameFetcher.apply(annotation);
      if (!ConnectorProcessor.None.class.equals(relativeProcessorClass)) {
        return Optional.of(findUniqueConnectorForClass(relativeProcessorClass));
      }
      if (Strings.isNotBlank(relativeProcessorName)) {
        final var element = processorRegistry.get(relativeProcessorName);
        SIPFrameworkInitializationException.throwOn(
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

    return matchingProcessor.orElseThrow(
        () ->
            SIPFrameworkInitializationException.init(
                "No processor found that matched the relative placement restriction for processor-class '%s' in %s",
                clazz.getName(), definingElement.getClass().getName()));
  }

  private Optional<Integer> readAbsolutePosition() {
    return definingElement.isAnnotationPresent(ExecuteOrder.class)
        ? Optional.of(definingElement.getAnnotation(ExecuteOrder.class).value())
        : Optional.empty();
  }
}
