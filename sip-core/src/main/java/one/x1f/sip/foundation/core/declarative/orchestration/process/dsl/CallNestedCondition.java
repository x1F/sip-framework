package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.StepResultCloner;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessStepConditional;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;

/**
 * DSL class used for construction conditional calls after main condition
 *
 * @param <R> DSL handle for the return DSL Verb/type.
 */
public final class CallNestedCondition<R> extends ProcessDslBase<CallNestedCondition<R>, R>
    implements CallableWithinProcessDefinition {

  @Getter(AccessLevel.PACKAGE)
  private final List<ProcessBranchStatements> conditionalStatements = new ArrayList<>();

  @Getter(AccessLevel.PACKAGE)
  private final List<CallableWithinProcessDefinition> unconditionalStatements = new ArrayList<>();

  private final CompositeProcessDefinition processDefinition;

  @Getter(AccessLevel.PACKAGE)
  private Optional<StepResultCloner<Object>> stepResultCloner = Optional.empty();

  CallNestedCondition(R dslReturnDefinition, CompositeProcessDefinition compositeProcess) {
    super(dslReturnDefinition, compositeProcess);
    this.processDefinition = compositeProcess;
  }

  ProcessBranch<CallNestedCondition<R>> elseIfCase(
      final CompositeProcessStepConditional predicate) {
    final var branch = new ProcessBranchStatements(predicate, new ArrayList<>());
    conditionalStatements.add(branch);
    return new ProcessBranch<>(branch.statements, self(), processDefinition);
  }

  ProcessBranch<R> elseCase() {
    return new ProcessBranch<>(
        unconditionalStatements, getDslReturnDefinition(), processDefinition);
  }

  R endCases() {
    return getDslReturnDefinition();
  }

  public record ProcessBranchStatements(
      CompositeProcessStepConditional predicate,
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
     * Standard if-else clause defined in the DSL way
     *
     * @param predicate Predicate to be checked for condition
     * @return DSL Handle
     */
    public ProcessBranch<CallNestedCondition<R>> elseIfCase(
        final CompositeProcessStepConditional predicate) {
      return CallNestedCondition.this.elseIfCase(predicate);
    }

    /**
     * Standard else clause defined in the DSL way
     *
     * @return DSL Handle
     */
    public ProcessBranch<R> elseCase() {
      return CallNestedCondition.this.elseCase();
    }

    /**
     * Standard end clause which closes the current conditional
     *
     * @return DSL Handle
     */
    public R endCases() {
      return CallNestedCondition.this.endCases();
    }
  }
}
