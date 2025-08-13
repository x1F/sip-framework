package one.x1f.sip.foundation.core.declarative.model;

import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorDefinition;
import one.x1f.sip.foundation.core.declarative.connector.ConnectorProcessor;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.model.RouteDefinition;

/**
 * Base class for transformers based on {@link ModelMapper}
 *
 * <p><em>For internal use only</em>
 *
 * @param <S> Source model type
 * @param <T> Target model type
 */
@Accessors(chain = true)
abstract sealed class BaseMappingRouteTransformer<S, T>
    implements Consumer<RouteDefinition>, ConnectorProcessor
    permits RequestMappingRouteTransformer, ResponseMappingRouteTransformer {

  @Getter(AccessLevel.PROTECTED)
  private final Supplier<ConnectorDefinition> connector;

  @Getter(AccessLevel.PROTECTED)
  private final Supplier<IntegrationScenarioDefinition> scenario;

  @Delegate(types = ConnectorProcessor.class)
  @Getter
  private final ModelMapper<S, T> mapper;

  protected BaseMappingRouteTransformer(
      final Supplier<ConnectorDefinition> connector,
      final Supplier<IntegrationScenarioDefinition> scenario,
      ModelMapper<S, T> mapper) {
    this.connector = connector;
    this.scenario = scenario;
    this.mapper = mapper;
  }

  @Override
  public final void accept(final RouteDefinition routeDefinition) {
    buildTransformerRoute(routeDefinition);
  }

  private void buildTransformerRoute(final RouteDefinition routeDefinition) {
    final var modelMapper = getMapper();
    if (notCompatibleTypes(modelMapper.getSourceModelClass(), getSourceModelClass()))
      throw newExceptionForIncompatibleTypes(
          modelMapper, "source", modelMapper.getSourceModelClass(), getSourceModelClass());
    if (notCompatibleTypes(modelMapper.getTargetModelClass(), getTargetModelClass()))
      throw newExceptionForIncompatibleTypes(
          modelMapper, "target", modelMapper.getTargetModelClass(), getTargetModelClass());

    routeDefinition.transform().method(modelMapper, ModelMapper.MAPPING_METHOD_NAME);
  }

  protected abstract Class<S> getSourceModelClass();

  protected abstract Class<T> getTargetModelClass();

  private boolean notCompatibleTypes(final Class<?> mapperType, final Class<?> assignedType) {
    return !mapperType.isAssignableFrom(assignedType);
  }

  private SIPFrameworkInitializationException newExceptionForIncompatibleTypes(
      ModelMapper<?, ?> modelMapper,
      String direction,
      Class<?> mapperType,
      final Class<?> assignedType) {
    return SIPFrameworkInitializationException.init(
        "Mapper '%s' %s type '%s' is not compatible with assigned type '%s' of connector '%s'",
        modelMapper.getClass().getName(),
        direction,
        mapperType.getName(),
        assignedType.getName(),
        connector.get().getId());
  }

  protected SIPFrameworkInitializationException getExceptionForMissingMapper() {
    return SIPFrameworkInitializationException.init(
        "No compatible Mapper found for Connector '%s' to map between %s and %s",
        connector.get().getId(), getSourceModelClass().getName(), getTargetModelClass().getName());
  }
}
