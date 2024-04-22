package de.ikor.sip.foundation.core.declarative.orchestration.scenario.dsl;

import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;

/** Class specifying consumer calls that are executed in a while loop */
public final class WhileLoopCallScenarioConsumerDefinition<R, M>
    extends ScenarioDslDefinitionBase<WhileLoopCallScenarioConsumerDefinition<R, M>, R, M>
    implements CallableWithinProviderDefinition {

  @Getter(AccessLevel.PACKAGE)
  private final List<BranchStatements<M>> loopStatements = new ArrayList<>();

  WhileLoopCallScenarioConsumerDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }

  protected Branch<WhileLoopCallScenarioConsumerDefinition<R, M>> doWhile(
      final Predicate<ScenarioOrchestrationContext<M>> predicate) {
    final var branch = new BranchStatements<>(predicate, new ArrayList<>());
    loopStatements.add(branch);
    return new Branch<>(branch.statements, self(), getIntegrationScenario());
  }

  R endLoop() {
    return getDslReturnDefinition();
  }

  record BranchStatements<M>(
      Predicate<ScenarioOrchestrationContext<M>> predicate,
      List<CallableWithinProviderDefinition> statements) {}

  public final class Branch<I> extends ScenarioDslDefinitionBase<Branch<I>, I, M>
      implements ScenarioConsumerCalls<Branch<I>, I, M> {

    @Delegate private final ScenarioConsumerCallsDelegate<Branch<I>, I, M> delegate;

    Branch(
        final List<CallableWithinProviderDefinition> statementsList,
        final I dslReturnDefinition,
        final IntegrationScenarioDefinition integrationScenario) {
      super(dslReturnDefinition, integrationScenario);
      delegate =
          new ScenarioConsumerCallsDelegate<>(
              statementsList, self(), getDslReturnDefinition(), getIntegrationScenario());
    }

    protected Branch<WhileLoopCallScenarioConsumerDefinition<R, M>> doWhile(
        final Predicate<ScenarioOrchestrationContext<M>> predicate) {
      return WhileLoopCallScenarioConsumerDefinition.this.doWhile(predicate);
    }

    /**
     * Ends the loop and returns to the previous scope.
     *
     * @return Previous scope of the orchestration definition
     */
    public R endLoop() {
      return WhileLoopCallScenarioConsumerDefinition.this.endLoop();
    }
  }
}
