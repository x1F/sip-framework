package de.ikor.sip.foundation.core.declarative.connector;

import de.ikor.sip.foundation.core.declarative.DeclarationsRegistryApi;
import de.ikor.sip.foundation.core.declarative.annonation.ConfigurationHandler;
import de.ikor.sip.foundation.core.declarative.annonation.UseRequestModelMapper;
import de.ikor.sip.foundation.core.declarative.annonation.UseResponseModelMapper;
import de.ikor.sip.foundation.core.declarative.model.FindAutomaticModelMapper;
import de.ikor.sip.foundation.core.declarative.model.RequestMappingRouteTransformer;
import de.ikor.sip.foundation.core.declarative.model.ResponseMappingRouteTransformer;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.connector.ConnectorOrchestrator;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeHelper;
import de.ikor.sip.foundation.core.declarative.utils.DeclarativeReflectionUtils;
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

  private final Optional<ConfigurationHandler> declarativeConfigurationAnnotation =
      DeclarativeReflectionUtils.getAnnotationIfPresent(ConfigurationHandler.class, this);

  private final List<Method> onExceptionHandlers =
      DeclarativeReflectionUtils.findAnnotatedMethodsWithReturnType(this.getClass());

  @Getter(AccessLevel.PROTECTED)
  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Getter(AccessLevel.PROTECTED)
  private ApplicationContext applicationContext;

  @Delegate
  private final Orchestrator<ConnectorOrchestrationInfo> modelTransformationOrchestrator =
      defineTransformationOrchestrator();

  private ResponseMappingRouteTransformer<Object, Object> responseMappingRouteTransformer;
  private RequestMappingRouteTransformer<Object, Object> requestMappingRouteTransformer;

  @Override
  public Orchestrator<ConnectorOrchestrationInfo> getOrchestrator() {
    return modelTransformationOrchestrator;
  }

  /**
   * Defines the {@link Orchestrator} for the transformation between connector and common domain
   * models. It is typically meant to be overridden, as the base implementation returns a simple
   * {@link ConnectorOrchestrator} which does not contain any additional model transformation logic.
   * It is only suitable if the connectors and the common domain model share the same type.
   *
   * @return Orchestrator for the transformation between connector and common domain models.
   */
  protected Orchestrator<ConnectorOrchestrationInfo> defineTransformationOrchestrator() {
    final var orchestrator = ConnectorOrchestrator.forConnector(this);
    getRequestMapper().ifPresent(orchestrator::setRequestRouteTransformer);
    getResponseMapper().ifPresent(orchestrator::setResponseRouteTransformer);
    return orchestrator;
  }

  @SuppressWarnings("unchecked")
  public Optional<RequestMappingRouteTransformer<Object, Object>> getRequestMapper() {
    if (requestMappingRouteTransformer != null) {
      return Optional.of(requestMappingRouteTransformer);
    }
    final var annotation =
        DeclarativeReflectionUtils.getAnnotationIfPresent(UseRequestModelMapper.class, this);
    if (annotation.isPresent()) {
      requestMappingRouteTransformer =
          RequestMappingRouteTransformer.forConnectorWithScenario(this, getScenario());
      if (!FindAutomaticModelMapper.class.equals(annotation.get().value())) {
        requestMappingRouteTransformer.setMapper(
            Optional.of(DeclarativeHelper.createMapperInstance(annotation.get().value())));
      }
      return Optional.of(requestMappingRouteTransformer);
    }
    return Optional.empty();
  }

  @SuppressWarnings("unchecked")
  public Optional<ResponseMappingRouteTransformer<Object, Object>> getResponseMapper() {
    if (responseMappingRouteTransformer != null) {
      return Optional.of(responseMappingRouteTransformer);
    }
    final var annotation =
        DeclarativeReflectionUtils.getAnnotationIfPresent(UseResponseModelMapper.class, this);
    if (annotation.isPresent()) {
      responseMappingRouteTransformer =
          ResponseMappingRouteTransformer.forConnectorWithScenario(this, getScenario());
      if (!FindAutomaticModelMapper.class.equals(annotation.get().value())) {
        responseMappingRouteTransformer.setMapper(
            Optional.of(DeclarativeHelper.createMapperInstance(annotation.get().value())));
      }
      return Optional.of(responseMappingRouteTransformer);
    }
    return Optional.empty();
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
