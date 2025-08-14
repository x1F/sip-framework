package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.StepResultCloner;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/** DSL base class for specifying the call to an integration scenario consumer */
public abstract sealed class CallScenarioConsumerBaseDefinition<
        S extends CallScenarioConsumerBaseDefinition<S, R, M>, R, M>
    extends ScenarioDslDefinitionBase<S, R, M> implements CallableWithinProviderDefinition
    permits CallScenarioConsumerByClassDefinition,
        CallScenarioConsumerByConnectorIdDefinition,
        CallScenarioConsumerCatchAllDefinition {

  @Getter(AccessLevel.PACKAGE)
  private Optional<ScenarioStepRequestExtractor<M>> requestPreparation = Optional.empty();

  @Getter(AccessLevel.PACKAGE)
  private Optional<ScenarioStepResponseConsumer<M>> responseConsumer = Optional.empty();

  @Getter(AccessLevel.PACKAGE)
  private Optional<StepResultCloner<M>> stepResultCloner = Optional.empty();

  CallScenarioConsumerBaseDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }

  /**
   * Attaches a {@link ScenarioStepRequestExtractor} that allows to manipulate the request for this
   * call. The request is only modified for this consumer and does not affect any subsequent
   * consumer calls, which will receive the original request by default.
   *
   * @param requestPreparation the extractor for the request
   * @return DSL handle
   */
  public S withRequestPreparation(final ScenarioStepRequestExtractor<M> requestPreparation) {
    this.requestPreparation = Optional.of(requestPreparation);
    return self();
  }

  /**
   * Declares that no specific response handling is required for this call. If the consumer provides
   * a response, it will not be modified.
   *
   * @return DSL handle
   */
  public R andNoResponseHandling() {
    return getDslReturnDefinition();
  }

  /**
   * Attaches an {@link ScenarioStepResponseAggregator} that allows to integrate the response of
   * this consumer call with the overall response for the orchestration.
   *
   * <p>This is a terminal operation that will finish the definition of this consumer call.
   *
   * @param responseAggregator The aggregator to be used with the response
   * @return DSL handle
   */
  public R andAggregateResponse(final ScenarioStepResponseAggregator<M> responseAggregator) {
    return andHandleResponse(
        (latestResponse, context) ->
            context.setAggregatedResponse(
                responseAggregator.aggregateResponse(
                    latestResponse, context.getAggregatedResponse()),
                getStepResultCloner()));
  }

  /**
   * Attaches a {@link ScenarioStepResponseConsumer} that allows to manipulate the response of this
   * consumer call.
   *
   * <p>If the intention of the response handling is to aggregate the response into an overall
   * response, it is recommended to use {@link
   * #andAggregateResponse(ScenarioStepResponseAggregator)} instead.
   *
   * <p>This is a terminal operation that will finish the definition of this consumer call.
   *
   * @param responseConsumer Consumer that handles the response
   * @return DSL handle
   */
  public R andHandleResponse(final ScenarioStepResponseConsumer<M> responseConsumer) {
    this.responseConsumer = Optional.of(responseConsumer);
    return getDslReturnDefinition();
  }

  /**
   * Attaches a {@link StepResultCloner} that allows to clone the response of this consumer call at
   * it is stored in the {@link ScenarioOrchestrationContext}.
   *
   * @param cloner The cloner to be used with the response
   * @return DSL handle
   */
  public S withResponseCloner(final StepResultCloner<M> cloner) {
    this.stepResultCloner = Optional.of(cloner);
    return self();
  }
}
