package one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.core.declarative.DeclarationsRegistryApi;
import one.x1f.sip.foundation.core.declarative.RoutesRegistry;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import org.apache.camel.model.RoutesDefinition;

/**
 * Base class for generating Camel routes from a DSL
 *
 * <p><em>For internal use only</em>
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class RouteGeneratorProcessBase {
  @Getter(AccessLevel.PROTECTED)
  private final CompositeProcessOrchestrationInfo orchestrationInfo;

  @Getter(value = AccessLevel.PROTECTED, lazy = true)
  private final RoutesDefinition routesDefinition = orchestrationInfo.getRoutesDefinition();

  @Getter(value = AccessLevel.PROTECTED, lazy = true)
  private final DeclarationsRegistryApi declarationsRegistry =
      getRoutesDefinition()
          .getCamelContext()
          .getRegistry()
          .findSingleByType(DeclarationsRegistryApi.class);

  @Getter(value = AccessLevel.PROTECTED, lazy = true)
  private final RoutesRegistry routesRegistry =
      getRoutesDefinition().getCamelContext().getRegistry().findSingleByType(RoutesRegistry.class);

  protected String getCompositeProcessId() {
    return getCompositeProcess().getId();
  }

  protected CompositeProcessDefinition getCompositeProcess() {
    return getOrchestrationInfo().getCompositeProcess();
  }
}
