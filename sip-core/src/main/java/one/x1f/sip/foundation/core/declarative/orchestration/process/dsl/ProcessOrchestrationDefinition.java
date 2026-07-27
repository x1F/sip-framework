package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.EndOfDsl;
import one.x1f.sip.foundation.core.declarative.orchestration.process.*;
import one.x1f.sip.foundation.core.declarative.process.CompositeProcessDefinition;

/** DSL class for specifying orchestration of complex processes with or without conditions */
public class ProcessOrchestrationDefinition
    extends ProcessDslBase<ProcessOrchestrationDefinition, EndOfDsl>
    implements ProcessConsumerCalls<ProcessOrchestrationDefinition, EndOfDsl> {

  @Getter(AccessLevel.PACKAGE)
  private final List<CallableWithinProcessDefinition> steps = new ArrayList<>();

  @Getter(AccessLevel.PACKAGE)
  private final List<CompositeProcessStepConditional> conditionals = new ArrayList<>();

  @Delegate
  @Getter(AccessLevel.PACKAGE)
  private final ForProcessProvidersDelegate<ProcessOrchestrationDefinition, EndOfDsl>
      forProcessProvidersDelegate =
          new ForProcessProvidersDelegate<>(steps, self(), getDslReturnDefinition());

  /**
   * Constructor
   *
   * <p><em>For internal use only</em>
   *
   * @param compositeProcess Composite Process
   */
  public ProcessOrchestrationDefinition(final CompositeProcessDefinition compositeProcess) {
    super(null, compositeProcess);
  }

  /**
   * Standard if clause defined in the DSL way
   *
   * @param predicate Predicate to be checked for condition
   * @return DSL Handle
   */
  public CallNestedCondition<ProcessOrchestrationDefinition>.ProcessBranch<
          CallNestedCondition<ProcessOrchestrationDefinition>>
      ifCase(final CompositeProcessStepConditional predicate) {
    final CallNestedCondition<ProcessOrchestrationDefinition> def =
        new CallNestedCondition<>(self(), getCompositeProcess());
    steps.add(def);
    conditionals.add(predicate);
    return def.elseIfCase(predicate);
  }

  /**
   * Standard while loop defined in the DSL way
   *
   * @param predicate Predicate to check when to stop while loop
   * @return DSL Handle
   */
  public CallWhileLoopStatement<ProcessOrchestrationDefinition>.ProcessBranch<
          CallWhileLoopStatement<ProcessOrchestrationDefinition>>
      doWhile(final CompositeProcessStepConditional predicate) {
    final CallWhileLoopStatement<ProcessOrchestrationDefinition> def =
        new CallWhileLoopStatement<>(self(), getCompositeProcess());
    steps.add(def);
    return def.doWhile(predicate);
  }

  /**
   * Standard for loop defined in the DSL way
   *
   * @param expression Expression to determine iteration number
   * @return DSL Handle
   */
  public CallForLoopStatement<ProcessOrchestrationDefinition>.ProcessBranch<
          CallForLoopStatement<ProcessOrchestrationDefinition>>
      forLoop(CompositeProcessStepIterations expression) {
    final CallForLoopStatement<ProcessOrchestrationDefinition> def =
        new CallForLoopStatement<>(self(), getCompositeProcess());
    steps.add(def);
    return def.forLoop(expression);
  }

  /**
   * Standard for split defined in the DSL way
   *
   * @param expression Expression to determine array to split
   * @return DSL Handle
   */
  public <T>
      CallSplitStatement<ProcessOrchestrationDefinition>.ProcessBranch<
              CallSplitStatement<ProcessOrchestrationDefinition>>
          split(CompositeProcessStepSplitExpression<T> expression) {
    final CallSplitStatement<ProcessOrchestrationDefinition> def =
        new CallSplitStatement<>(self(), getCompositeProcess());
    steps.add(def);
    return def.split(expression);
  }

  public <T>
      CallSplitStatement<ProcessOrchestrationDefinition>.ProcessBranch<
              CallSplitStatement<ProcessOrchestrationDefinition>>
          parallelSplit(CompositeProcessStepSplitExpression<T> expression) {
    final CallSplitStatement<ProcessOrchestrationDefinition> def =
        new CallSplitStatement<>(self(), getCompositeProcess());
    steps.add(def);
    return def.parallelSplit(expression);
  }

  public ProcessOrchestrationDefinition process(CompositeProcessTransformer requestPreparation) {
    CallProcess<? extends CallProcess<?, ?>, ProcessOrchestrationDefinition> def =
        new CallProcess(self(), getCompositeProcess());
    steps.add(def);
    return def.process(requestPreparation);
  }
}
