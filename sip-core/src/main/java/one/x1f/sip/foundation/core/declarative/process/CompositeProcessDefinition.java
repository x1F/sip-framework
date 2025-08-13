package one.x1f.sip.foundation.core.declarative.process;

import java.util.List;
import one.x1f.sip.foundation.core.declarative.DeclarativeElement;
import one.x1f.sip.foundation.core.declarative.annotation.CompositeProcess;
import one.x1f.sip.foundation.core.declarative.orchestration.Orchestratable;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioProviderDefinition;

/**
 * Interface used for specifying composite processes used within a SIP adapter.
 *
 * <p>Composite process allows for connecting multiple integration scenarios into one process.
 * Process can be provided data from the integration scenario in the same way as the outbound
 * connectors do. Process can send data to be consumed by integration scenarios in the same way as
 * the inbound connectors do. This allows making composite and complex flows for integration
 * scenarios where one side of the integration scenario can not be fulfilled by a simple connector.
 *
 * <p><em>Adapter developers should not implement this interface directly, but use {@link
 * CompositeProcessBase} instead.</em>
 *
 * @see CompositeProcessBase
 * @see CompositeProcess
 */
public interface CompositeProcessDefinition
    extends Orchestratable<CompositeProcessOrchestrationInfo>,
        IntegrationScenarioProviderDefinition,
        IntegrationScenarioConsumerDefinition,
        DeclarativeElement {

  List<Class<? extends IntegrationScenarioDefinition>> getConsumerDefinitions();

  Class<? extends IntegrationScenarioDefinition> getProviderDefinition();
}
