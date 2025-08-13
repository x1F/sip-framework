package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;

/**
 * DSL class for calling a scenario consumer specified by it's class for scenarios without response
 */
public final class CallScenarioConsumerBaseNoResponseDefinition<R, M>
    extends CallScenarioConsumerByClassDefinition<R, M> {

  CallScenarioConsumerBaseNoResponseDefinition(
      R dslReturnDefinition,
      IntegrationScenarioDefinition integrationScenario,
      Class<? extends IntegrationScenarioConsumerDefinition> consumerClass) {
    super(dslReturnDefinition, integrationScenario, consumerClass);
  }

  @Override
  public R andHandleResponse(ScenarioStepResponseConsumer<M> responseConsumer) {
    throw SIPFrameworkInitializationException.init(
        "Integration Scenario %s does not have a response model defined, using response handler is not intended.",
        getIntegrationScenario().getId());
  }

  @Override
  public R andAggregateResponse(final ScenarioStepResponseAggregator<M> responseAggregator) {
    throw SIPFrameworkInitializationException.init(
        "Integration Scenario %s does not have a response model defined, using aggregate response is not intended.",
        getIntegrationScenario().getId());
  }
}
