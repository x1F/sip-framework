package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationHandlers;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessTransformer;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.CallProcess;
import one.x1f.sip.foundation.core.declarative.orchestration.process.dsl.RouteGeneratorInternalHelper;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkInitializationException;
import org.apache.camel.model.ProcessorDefinition;

/**
 * Class for generating Camel routes for process consumer calls from a DSL
 *
 * <p><em>For internal use only</em>
 */
@Slf4j
@SuppressWarnings("rawtypes")
final class RouteGeneratorForProcessTransformer extends RouteGeneratorProcessBase {

  private final CallProcess<?, ?> definitionElement;

  RouteGeneratorForProcessTransformer(
      final CompositeProcessOrchestrationInfo orchestrationInfo,
      final CallProcess definitionElement) {
    super(orchestrationInfo);
    this.definitionElement = definitionElement;
  }

  <T extends ProcessorDefinition<T>> void generateRoute(final T routeDefinition) {
    Optional<CompositeProcessTransformer> compositeProcessTransformer =
        RouteGeneratorInternalHelper.getProcess(definitionElement);
    if (compositeProcessTransformer.isEmpty()) {
      throw SIPFrameworkInitializationException.init(
          "Empty process statement attached in orchestration for composite process '%s'",
          getCompositeProcessId());
    }
    routeDefinition.process(
        exchange -> {
          CompositeProcessOrchestrationHandlers.handleProcess(
              exchange,
              Optional.empty(),
              RouteGeneratorInternalHelper.getProcess(definitionElement));
        });
  }
}
