package de.ikor.sip.foundation.core.declarative.orchestration.process;

import java.util.List;

@FunctionalInterface
public interface CompositeProcessStepSplitExpression {


  List<?> determinePayload(CompositeProcessOrchestrationContext context);
}
