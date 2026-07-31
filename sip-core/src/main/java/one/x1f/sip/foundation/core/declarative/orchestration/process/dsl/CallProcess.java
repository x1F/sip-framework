package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessTransformer;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;

/**
 * DSL class used for construction conditional calls after main condition
 *
 * @param <R> DSL handle for the return DSL Verb/type.
 */
public final class CallProcess<R> extends ProcessDslBase<CallProcess<R>, R>
    implements CallableWithinProcessDefinition {

  @Getter(AccessLevel.PACKAGE)
  private Optional<CompositeProcessTransformer> process = Optional.empty();

  CallProcess(R dslReturnDefinition, CompositeProcessDefinition compositeProcess) {
    super(dslReturnDefinition, compositeProcess);
  }

  R process(final CompositeProcessTransformer expression) {
    process = Optional.of(expression);
    return getDslReturnDefinition();
  }
}
