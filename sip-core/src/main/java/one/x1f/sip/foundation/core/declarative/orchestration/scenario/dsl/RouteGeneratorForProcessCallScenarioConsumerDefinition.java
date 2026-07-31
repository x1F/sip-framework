package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationHandlers;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import org.apache.camel.model.ProcessorDefinition;

@SuppressWarnings("rawtypes")
@Slf4j
final class RouteGeneratorForProcessCallScenarioConsumerDefinition<M> extends RouteGeneratorBase {
  private final ProcessCallScenarioConsumerDefinition<?, M> processDefinition;

  RouteGeneratorForProcessCallScenarioConsumerDefinition(
      final ScenarioOrchestrationInfo orchestrationInfo,
      final ProcessCallScenarioConsumerDefinition<?, M> processDefinition) {
    super(orchestrationInfo);
    this.processDefinition = processDefinition;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    routeDefinition.process(
        exchange -> ScenarioOrchestrationHandlers.handleContextConsumer(
            processDefinition.getConsumer(), exchange));
  }
}
