package de.ikor.sip.foundation.core.declarative.orchestration.process.dsl;

import de.ikor.sip.foundation.core.declarative.orchestration.common.dsl.EndOfDsl;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessStepConditional;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;

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

  public CallLoopStatement<ProcessOrchestrationDefinition>.ProcessBranch<
          CallLoopStatement<ProcessOrchestrationDefinition>>
      doWhile(final CompositeProcessStepConditional predicate) {
    final CallLoopStatement<ProcessOrchestrationDefinition> def =
        new CallLoopStatement<>(self(), getCompositeProcess());
    steps.add(def);
    conditionals.add(predicate);
    return def.doWhile(predicate);
  }
}
