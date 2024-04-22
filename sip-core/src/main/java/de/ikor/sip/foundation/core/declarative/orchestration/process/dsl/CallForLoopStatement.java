package de.ikor.sip.foundation.core.declarative.orchestration.process.dsl;

import de.ikor.sip.foundation.core.declarative.orchestration.common.dsl.StepResultCloner;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessStepIterations;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;

/**
 * DSL class used for construction conditional calls after main condition
 *
 * @param <R> DSL handle for the return DSL Verb/type.
 */
public final class CallForLoopStatement<R> extends ProcessDslBase<CallForLoopStatement<R>, R>
    implements CallableWithinProcessDefinition {

  @Getter(AccessLevel.PACKAGE)
  private List<ProcessBranchStatements> loopProcess = new ArrayList<>();

  private final CompositeProcessDefinition processDefinition;

  @Getter(AccessLevel.PACKAGE)
  private Optional<StepResultCloner<Object>> stepResultCloner = Optional.empty();

  CallForLoopStatement(R dslReturnDefinition, CompositeProcessDefinition compositeProcess) {
    super(dslReturnDefinition, compositeProcess);
    this.processDefinition = compositeProcess;
  }

  ProcessBranch<CallForLoopStatement<R>> forLoop(final CompositeProcessStepIterations interations) {
    final var branch = new ProcessBranchStatements(interations, new ArrayList<>());
    loopProcess.add(branch);
    return new ProcessBranch<>(branch.statements, self(), processDefinition);
  }

  R endLoop() {
    return getDslReturnDefinition();
  }

  public record ProcessBranchStatements(
      CompositeProcessStepIterations iterations,
      List<CallableWithinProcessDefinition> statements) {}

  public final class ProcessBranch<I> extends ProcessDslBase<ProcessBranch<I>, I>
      implements ProcessConsumerCalls<ProcessBranch<I>, I> {

    @Delegate private final ForProcessProvidersDelegate<ProcessBranch<I>, I> delegate;

    ProcessBranch(
        final List<CallableWithinProcessDefinition> statementsList,
        final I dslReturnDefinition,
        final CompositeProcessDefinition processDefinition) {
      super(dslReturnDefinition, processDefinition);
      delegate =
          new ForProcessProvidersDelegate<>(statementsList, self(), getDslReturnDefinition());
    }

    public R endLoop() {
      return CallForLoopStatement.this.endLoop();
    }
  }
}
