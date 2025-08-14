package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessStepSplitExpression;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;

/**
 * DSL class used for construction of for split statements and ending them
 *
 * @param <R> DSL handle for the return DSL Verb/type.
 */
public final class CallSplitStatement<R> extends ProcessDslBase<CallSplitStatement<R>, R>
    implements CallableWithinProcessDefinition {

  @Getter(AccessLevel.PACKAGE)
  private final List<ProcessBranchStatements> splitProcess = new ArrayList<>();

  private final CompositeProcessDefinition processDefinition;
  @Getter private boolean isParallel = false;

  CallSplitStatement(R dslReturnDefinition, CompositeProcessDefinition compositeProcess) {
    super(dslReturnDefinition, compositeProcess);
    this.processDefinition = compositeProcess;
  }

  <T> ProcessBranch<CallSplitStatement<R>> split(
      final CompositeProcessStepSplitExpression<T> expression) {
    final var branch = new ProcessBranchStatements(expression, new ArrayList<>());
    splitProcess.add(branch);
    return new ProcessBranch<>(branch.statements, self(), processDefinition);
  }

  <T> ProcessBranch<CallSplitStatement<R>> parallelSplit(
      final CompositeProcessStepSplitExpression<T> expression) {
    isParallel = true;
    final var branch = new ProcessBranchStatements(expression, new ArrayList<>());
    splitProcess.add(branch);
    return new ProcessBranch<>(branch.statements, self(), processDefinition);
  }

  R endSplit() {
    return getDslReturnDefinition();
  }

  public record ProcessBranchStatements(
      CompositeProcessStepSplitExpression<?> expression,
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

    /**
     * End current split statement and return to previous process building DSL
     *
     * @return previous process building DSL
     */
    public R endSplit() {
      return CallSplitStatement.this.endSplit();
    }
  }
}
