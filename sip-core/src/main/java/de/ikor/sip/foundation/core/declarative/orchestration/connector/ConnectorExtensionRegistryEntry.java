package de.ikor.sip.foundation.core.declarative.orchestration.connector;

import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.ExecuteAfter;
import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.ExecuteBefore;
import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.ExecutionOrder;
import de.ikor.sip.foundation.core.declarative.connector.ConnectorExtension;
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
class ConnectorExtensionRegistryEntry {

  @ToString.Include @EqualsAndHashCode.Include ConnectorExtension extension;
  @EqualsAndHashCode.Include @ToString.Include AnnotatedElement definingElement;
  Map<String, ConnectorExtensionRegistryEntry> extensionRegistry;

  @Getter(lazy = true)
  boolean placedFirst = resolvePlacedFirst();

  @Getter(lazy = true)
  boolean placedLast = resolvePlacedLast();

  @Getter(lazy = true)
  Optional<Integer> placementAbsolute = resolveAbsolutePosition();

  @Getter(lazy = true)
  Optional<ConnectorExtension> placementBeforeExtension =
      resolveRelativePlacedExtension(
          ExecuteBefore.class, ExecuteBefore::value, ExecuteBefore::extensionName);

  @Getter(lazy = true)
  Optional<ConnectorExtension> placementAfterExtension =
      resolveRelativePlacedExtension(
          ExecuteAfter.class, ExecuteAfter::value, ExecuteAfter::extensionName);

  @ToString.Include
  public String getExtensionName() {
    return extension.getExtensionName();
  }

  private boolean resolvePlacedFirst() {
    return DeclarativeReflectionUtils.getAnnotationIfPresent(ExecutionOrder.class, definingElement)
        .map(ExecutionOrder::first)
        .orElse(false);
  }

  private boolean resolvePlacedLast() {
    return DeclarativeReflectionUtils.getAnnotationIfPresent(ExecutionOrder.class, definingElement)
        .map(ExecutionOrder::last)
        .orElse(false);
  }

  private Optional<Integer> resolveAbsolutePosition() {
    final var annotation =
        DeclarativeReflectionUtils.getAnnotationIfPresent(ExecutionOrder.class, definingElement);
    if (annotation.isPresent() && annotation.get().value() > -1) {
      return annotation.map(ExecutionOrder::value);
    }
    return Optional.empty();
  }

  private <T extends Annotation> Optional<ConnectorExtension> resolveRelativePlacedExtension(
      final Class<T> annotationClass,
      Function<T, Class<? extends ConnectorExtension>> extensionClassFetcher,
      Function<T, String> extensionNameFetcher) {
    final var annotation =
        DeclarativeReflectionUtils.getAnnotationIfPresent(annotationClass, definingElement);
    if (annotation.isPresent()) {
      final Class<? extends ConnectorExtension> relativeExtensionClass =
          extensionClassFetcher.apply(annotation.orElseThrow());
      final String relativeExtensionName = extensionNameFetcher.apply(annotation.orElseThrow());
      if (!ConnectorExtension.None.class.equals(relativeExtensionClass)) {
        return Optional.of(findUniqueConnectorForClass(relativeExtensionClass));
      }
      if (Strings.isNotBlank(relativeExtensionName)) {
        final var element = extensionRegistry.get(relativeExtensionName);
        SIPFrameworkInitializationException.throwIf(
            null == element,
            "No matching extension named '%s' could be found for relative placement defined in annotation %s in %s",
            relativeExtensionName,
            annotationClass.getSimpleName(),
            definingElement.getClass().getName());
        return Optional.of(element.getExtension());
      }
      throw SIPFrameworkInitializationException.init(
          "No placement specified in annotation @%s in class %s",
          annotationClass.getSimpleName(), definingElement.getClass().getName());
    }
    return Optional.empty();
  }

  private ConnectorExtension findUniqueConnectorForClass(
      final Class<? extends ConnectorExtension> clazz) {

    final var matchingExtension =
        StreamHelper.findAtMostOne(
            extensionRegistry.values().stream().map(ConnectorExtensionRegistryEntry::getExtension),
            proc -> clazz.equals(proc.getClass()),
            () ->
                SIPFrameworkInitializationException.init(
                    "More than one extension matched the relative placement restriction for extension-class '%s' in %s",
                    clazz.getName(), definingElement.getClass().getName()));

    matchingExtension.ifPresent(
        match ->
            SIPFrameworkInitializationException.throwIf(
                extension.equals(match),
                "Relative placement for connector extension '%s' is pointing on itself",
                extension.getExtensionName()));

    return matchingExtension.orElseThrow(
        () ->
            SIPFrameworkInitializationException.init(
                "No extension found that matched the relative placement restriction for extension-class '%s' in %s",
                clazz.getName(), definingElement.getClass().getName()));
  }
}
