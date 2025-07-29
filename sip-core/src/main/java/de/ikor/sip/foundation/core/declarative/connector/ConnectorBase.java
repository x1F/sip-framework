package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.DeclarationsRegistryApi;
import de.ikor.sip.foundation.core.declarative.annonation.ConfigurationHandler;
import de.ikor.sip.foundation.core.declarative.annonation.UseRequestModelMapper;
import de.ikor.sip.foundation.core.declarative.annonation.UseResponseModelMapper;
import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.RequestProcessor;
import de.ikor.sip.foundation.core.declarative.annotation.connector.extension.ResponseProcessor;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorExtensionChainOrchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ClassUtils;

/**
 * Base class for connector definitions.
 *
 * <p>This class provides a default implementation for the {@link Orchestrator} interface, and
 * allows subclasses to attach an {@link Orchestrator} for the transformation between connector and
 * common domain models through the {@link #defineTransformationOrchestrator()} method.
 */
public abstract non-sealed class ConnectorBase
    implements ConnectorDefinition, ApplicationContextAware {

  private static final List<Class<? extends Annotation>> TRANSFORM_OVERLOAD_PROHIBITED_ANNOTATIONS =
      List.of(UseRequestModelMapper.class, UseResponseModelMapper.class);

  private final Optional<ConfigurationHandler> declarativeConfigurationAnnotation =
      DeclarativeReflectionUtils.getAnnotationIfPresent(ConfigurationHandler.class, this);

  private final List<Method> onExceptionHandlers =
      DeclarativeReflectionUtils.findAnnotatedMethodsWithReturnType(this.getClass());

  @Getter(AccessLevel.PROTECTED)
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Getter(AccessLevel.PROTECTED)
  private ApplicationContext applicationContext;

  @Delegate private Orchestrator<ConnectorOrchestrationInfo> modelTransformationOrchestrator;

  @Override
  public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
    if (modelTransformationOrchestrator == null) {
      modelTransformationOrchestrator = initConnectorOrchestrator();
    }
    return modelTransformationOrchestrator;
  }

  public final Supplier<IntegrationScenarioDefinition> getScenario() {
    return () ->
        applicationContext.getBean(DeclarationsRegistryApi.class).getScenarioById(getScenarioId());
  }

  @Override
  public final void setApplicationContext(final ApplicationContext applicationContext)
      throws BeansException {
    this.applicationContext = applicationContext;
  }

  private Orchestrator<ConnectorOrchestrationInfo> initConnectorOrchestrator() {
    if (isDeprecatedTransformationOrchestrationOverloaded()) {
      return buildDeprecatedTransformationOverloadOrchestrator();
    }
    return ConnectorExtensionChainOrchestrator.builder()
        .relatedConnector(() -> this)
        .applicationContext(this::getApplicationContext)
        .build();
  }

  /**
   * Defines the {@link Orchestrator} for the transformation between connector and common domain
   * models. It is typically meant to be overridden, as the base implementation returns a simple
   * {@link ConnectorOrchestrator} which does not contain any additional model transformation logic.
   * It is only suitable if the connectors and the common domain model share the same type.
   *
   * @deprecated Use new connector-processor extensions via @{@link RequestProcessor}, and @{@link
   *     ResponseProcessor} instead
   * @return Orchestrator for the transformation between connector and common domain models.
   */
  @Deprecated(since = "3.4.0")
  protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
    return ConnectorOrchestrator.forConnector(this);
  }

  private boolean isDeprecatedTransformationOrchestrationOverloaded() {
    Class<?> checkClass = getClass();
    while (!ConnectorBase.class.equals(checkClass)) {
      var found =
          Arrays.stream(checkClass.getDeclaredMethods())
              .filter(method -> method.getName().equals("defineTransformationOrchestrator"))
              .findAny();
      if (found.isPresent()) {
        return true;
      }
      checkClass = checkClass.getSuperclass();
    }
    return false;
  }

  private Orchestrator<ConnectorOrchestrationInfo>
      buildDeprecatedTransformationOverloadOrchestrator() {
    getLogger()
        .warn(
            "Connector {} is overloading deprecated defineTransformationOrchestrator() method. Consider using connector-processor extensions instead.",
            getClass().getName());
    @Deprecated final var transformationOrchestrator = defineTransformationOrchestrator();

    if (transformationOrchestrator
        instanceof @SuppressWarnings("deprecation") ConnectorOrchestrator connectorOrchestrator) {
      DeclarativeReflectionUtils.getAnnotationIfPresent(UseRequestModelMapper.class, this)
          .ifPresent(
              transformer -> {
                throw SIPFrameworkInitializationException.init(
                    "Connector %s specifies custom request-transformation in it's orchestrator, and at the same time has annotation @%s present, which is not allowed.",
                    getClass().getName(), UseRequestModelMapper.class.getSimpleName());
              });
      DeclarativeReflectionUtils.getAnnotationIfPresent(UseResponseModelMapper.class, this)
          .ifPresent(
              transformer -> {
                throw SIPFrameworkInitializationException.init(
                    "Connector %s specifies custom response-transformation in it's orchestrator, and at the same time has annotation @%s present, which is not allowed.",
                    getClass().getName(), UseResponseModelMapper.class.getSimpleName());
              });
    } else {
      TRANSFORM_OVERLOAD_PROHIBITED_ANNOTATIONS.stream()
          .filter(annotation -> getClass().isAnnotationPresent(annotation))
          .findAny()
          .ifPresent(
              annotation -> {
                throw SIPFrameworkInitializationException.init(
                    "Connector %s is overloading method defineTransformationOrchestrator() and at the same time has annotation @%s present, which is not supported.",
                    getClass().getName(), annotation.getSimpleName());
              });
    }
    return transformationOrchestrator;
  }

  @Override
  public final String[] getConfigurationIds() {
    return declarativeConfigurationAnnotation
        .map(
            configurationHandler ->
                Arrays.stream(configurationHandler.value())
                    .map(ClassUtils::getShortName)
                    .toArray(String[]::new))
        .orElseGet(() -> new String[0]);
  }

  @Override
  public final List<Method> getOnExceptionHandler() {
    return onExceptionHandlers;
  }
}
