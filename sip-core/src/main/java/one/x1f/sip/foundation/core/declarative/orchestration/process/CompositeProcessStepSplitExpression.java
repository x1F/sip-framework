package one.x1f.sip.foundation.core.declarative.orchestration.process;

import java.util.Collection;

@FunctionalInterface
public interface CompositeProcessStepSplitExpression {

  Collection determinePayload(CompositeProcessOrchestrationContext context);
}
