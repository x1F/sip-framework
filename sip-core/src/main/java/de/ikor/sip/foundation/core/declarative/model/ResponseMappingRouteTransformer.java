package de.ikor.sip.foundation.core.declarative.model;

import de.ikor.sip.foundation.core.declarative.connector.ConnectorDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import java.util.function.Supplier;

/**
 * Response Model transformer used in conjunction with a {@link ModelMapper}.
 *
 * <p><em>For internal use only</em>
 */
public final class ResponseMappingRouteTransformer<S, T> extends BaseMappingRouteTransformer<S, T> {

  protected ResponseMappingRouteTransformer(
      final Supplier<ConnectorDefinition> connector,
      final Supplier<IntegrationScenarioDefinition> scenario,
      final ModelMapper<S, T> mapper) {
    super(connector, scenario, mapper);
  }

  public static <S, T> ResponseMappingRouteTransformer<S, T> forConnectorWithScenario(
      final ConnectorDefinition connector,
      final Supplier<IntegrationScenarioDefinition> scenario,
      final ModelMapper<S, T> mapper) {
    return forConnectorWithScenario(() -> connector, scenario, mapper);
  }

  public static <S, T> ResponseMappingRouteTransformer<S, T> forConnectorWithScenario(
      final Supplier<ConnectorDefinition> connector,
      final Supplier<IntegrationScenarioDefinition> scenario,
      final ModelMapper<S, T> mapper) {
    return new ResponseMappingRouteTransformer<>(connector, scenario, mapper);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Class<S> getSourceModelClass() {
    return switch (getConnector().get().getConnectorType()) {
      case IN -> (Class<S>) getScenario().get().getResponseModelClass().orElseThrow();
      case OUT -> (Class<S>) getConnector().get().getResponseModelClass().orElseThrow();
    };
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Class<T> getTargetModelClass() {
    return switch (getConnector().get().getConnectorType()) {
      case IN -> (Class<T>) getConnector().get().getResponseModelClass().orElseThrow();
      case OUT -> (Class<T>) getScenario().get().getResponseModelClass().orElseThrow();
    };
  }
}
