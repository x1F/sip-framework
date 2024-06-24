package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.DeclarationsRegistryApi;
import de.ikor.sip.foundation.core.declarative.annonation.UseRequestModelMapper;
import de.ikor.sip.foundation.core.declarative.annonation.UseResponseModelMapper;
import de.ikor.sip.foundation.core.declarative.model.RequestMappingRouteTransformer;
import de.ikor.sip.foundation.core.declarative.model.ResponseMappingRouteTransformer;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorProcessorChainOrchestrator;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import java.lang.annotation.Annotation;
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

  @Getter(AccessLevel.PROTECTED)
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Getter(AccessLevel.PROTECTED)
  private ApplicationContext applicationContext;

  @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
  private final Optional<RequestMappingRouteTransformer<?, ?>> requestMappingRouteTransformer =
      DeclarativeReflectionUtils.getAnnotationIfPresent(UseRequestModelMapper.class, this)
          .map(annotation -> DeclarativeHelper.createMapperInstance(annotation.value()))
          .map(
              mapper ->
                  RequestMappingRouteTransformer.forConnectorWithScenario(
                      this, getScenario(), mapper));

  @SuppressWarnings({"unchecked", "OptionalUsedAsFieldOrParameterType"})
  private final Optional<ResponseMappingRouteTransformer<?, ?>> responseMappingRouteTransformer =
      DeclarativeReflectionUtils.getAnnotationIfPresent(UseResponseModelMapper.class, this)
          .map(annotation -> DeclarativeHelper.createMapperInstance(annotation.value()))
          .map(
              mapper ->
                  ResponseMappingRouteTransformer.forConnectorWithScenario(
                      this, getScenario(), mapper));

  @Delegate
  private final Orchestrator<ConnectorOrchestrationInfo> modelTransformationOrchestrator =
      initConnectorOrchestrator();

  @Override
  public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
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
    return buildDeprecatedTransformationOverloadOrchestrator()
        .orElseGet(
            () ->
                ConnectorProcessorChainOrchestrator.builder()
                    .relatedConnector(() -> this)
                    .applicationContext(this::getApplicationContext)
                    .build());
  }

  /**
   * Defines the {@link Orchestrator} for the transformation between connector and common domain
   * models. It is typically meant to be overridden, as the base implementation returns a simple
   * {@link ConnectorOrchestrator} which does not contain any additional model transformation logic.
   * It is only suitable if the connectors and the common domain model share the same type.
   *
   * @deprecated Use new connector-processor extensions via @{@link
   *     de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorRequestProcessor}
   *     and @{@link
   *     de.ikor.sip.foundation.core.declarative.annotation.connector.ConnectorResponseProcessor}
   *     instead
   * @return Orchestrator for the transformation between connector and common domain models.
   */
  @Deprecated
  protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
    return defineStandardDeprecatedTransformationOrchestrator();
  }

  private Orchestrator<ConnectorOrchestrationInfo>
      defineStandardDeprecatedTransformationOrchestrator() {
    final var orchestrator = ConnectorOrchestrator.forConnector(this);
    requestMappingRouteTransformer.ifPresent(orchestrator::setRequestRouteTransformer);
    responseMappingRouteTransformer.ifPresent(orchestrator::setResponseRouteTransformer);
    return orchestrator;
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

  private Optional<Orchestrator<ConnectorOrchestrationInfo>>
      buildDeprecatedTransformationOverloadOrchestrator() {
    if (isDeprecatedTransformationOrchestrationOverloaded()) {
      final var transformationOrchestrator = defineTransformationOrchestrator();
      getLogger()
          .warn(
              "Connector {} is overloading deprecated defineTransformationOrchestrator() method. Consider using connector-processor extensions instead.",
              getClass().getName());

      if (transformationOrchestrator instanceof ConnectorOrchestrator connectorOrchestrator) {
        requestMappingRouteTransformer.ifPresent(
            transformer ->
                SIPFrameworkInitializationException.throwOn(
                    !transformer.equals(connectorOrchestrator.getRequestRouteTransformer()),
                    "Connector %s specifies custom request-transformation in it's orchestrator, and at the same time has annotation @%s present, which is not allowed.",
                    getClass().getName(),
                    UseRequestModelMapper.class.getSimpleName()));
        responseMappingRouteTransformer.ifPresent(
            transformer ->
                SIPFrameworkInitializationException.throwOn(
                    !transformer.equals(connectorOrchestrator.getResponseRouteTransformer()),
                    "Connector %s specifies custom response-transformation in it's orchestrator, and at the same time has annotation @%s present, which is not allowed.",
                    getClass().getName(),
                    UseResponseModelMapper.class.getSimpleName()));
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
      return Optional.of(transformationOrchestrator);
    }
    return Optional.empty();
  }
}
