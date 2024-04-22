package de.ikor.sip.foundation.core.declarative.orchestration.scenario.dsl;

import de.ikor.sip.foundation.core.declarative.orchestration.process.routebuilding.ScenarioStepIterations;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;

/** Class specifying consumer calls that are only executed conditionally */
public final class ForLoopCallScenarioConsumerDefinition<R, M>
    extends ScenarioDslDefinitionBase<ForLoopCallScenarioConsumerDefinition<R, M>, R, M>
    implements CallableWithinProviderDefinition {

  @Getter(AccessLevel.PACKAGE)
  private final List<BranchStatements> loopStatements = new ArrayList<>();

  ForLoopCallScenarioConsumerDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }

  protected Branch<ForLoopCallScenarioConsumerDefinition<R, M>> forLoop(
      final ScenarioStepIterations predicate) {
    final var branch = new BranchStatements<>(predicate, new ArrayList<>());
    loopStatements.add(branch);
    return new Branch<>(branch.statements, self(), getIntegrationScenario());
  }

  R endLoop() {
    return getDslReturnDefinition();
  }

  record BranchStatements<M>(
      ScenarioStepIterations predicate, List<CallableWithinProviderDefinition> statements) {}

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

    /**
     * Defines an alternative conditional branch that is executed if the given <code>iterations
     * </code> matches.
     *
     * @param predicate Predicate to test for execution of the branch
     * @return The conditional branch
     */
    protected Branch<ForLoopCallScenarioConsumerDefinition<R, M>> forLoop(
        final ScenarioStepIterations predicate) {
      return ForLoopCallScenarioConsumerDefinition.this.forLoop(predicate);
    }

    /**
     * Ends the condition and returns to the previous scope.
     *
     * @return Previous scope of the orchestration definition
     */
    public R endLoop() {
      return ForLoopCallScenarioConsumerDefinition.this.endLoop();
    }
  }
}
