package de.ikor.sip.foundation.core.declarative.model;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import java.util.function.Supplier;

/**
 * Request Model transformer used in conjunction with a {@link ModelMapper}.
 *
 * <p><em>For internal use only</em> *
 */
public final class RequestMappingRouteTransformer<S, T> extends BaseMappingRouteTransformer<S, T> {

  protected RequestMappingRouteTransformer(
      final Supplier<ConnectorDefinition> connector,
      final Supplier<IntegrationScenarioDefinition> scenario,
      final ModelMapper<S, T> mapper) {
    super(connector, scenario, mapper);
  }

  public static <S, T> RequestMappingRouteTransformer<S, T> forConnectorWithScenario(
      final ConnectorDefinition connector,
      final Supplier<IntegrationScenarioDefinition> scenario,
      final ModelMapper<S, T> mapper) {
    return forConnectorWithScenario(() -> connector, scenario, mapper);
  }

  public static <S, T> RequestMappingRouteTransformer<S, T> forConnectorWithScenario(
      final Supplier<ConnectorDefinition> connector,
      final Supplier<IntegrationScenarioDefinition> scenario,
      final ModelMapper<S, T> mapper) {
    return new RequestMappingRouteTransformer<>(connector, scenario, mapper);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Class<S> getSourceModelClass() {
    return switch (getConnector().get().getConnectorType()) {
      case IN -> (Class<S>) getConnector().get().getRequestModelClass();
      case OUT -> (Class<S>) getScenario().get().getRequestModelClass();
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Class<T> getTargetModelClass() {
    return switch (getConnector().get().getConnectorType()) {
      case IN -> (Class<T>) getScenario().get().getRequestModelClass();
      case OUT -> (Class<T>) getConnector().get().getRequestModelClass();
    };
  }
}
