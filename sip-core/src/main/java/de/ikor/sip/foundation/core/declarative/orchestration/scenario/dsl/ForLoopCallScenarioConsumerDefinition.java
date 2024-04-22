package de.ikor.sip.foundation.core.declarative.orchestration.scenario.dsl;

import de.ikor.sip.foundation.core.declarative.orchestration.process.routebuilding.ScenarioStepIterations;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;

/** Class specifying consumer calls that are executed in a for loop */
public final class ForLoopCallScenarioConsumerDefinition<R, M>
    extends ScenarioDslDefinitionBase<ForLoopCallScenarioConsumerDefinition<R, M>, R, M>
    implements CallableWithinProviderDefinition {

  @Getter(AccessLevel.PACKAGE)
  private final List<BranchStatements<M>> loopStatements = new ArrayList<>();

  ForLoopCallScenarioConsumerDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }

  Branch<ForLoopCallScenarioConsumerDefinition<R, M>> forLoop(
      final ScenarioStepIterations<M> predicate) {
    final var branch = new BranchStatements<>(predicate, new ArrayList<>());
    loopStatements.add(branch);
    return new Branch<>(branch.statements, self(), getIntegrationScenario());
  }

  R endLoop() {
    return getDslReturnDefinition();
  }

  record BranchStatements<M>(
      ScenarioStepIterations<M> predicate, List<CallableWithinProviderDefinition> statements) {}

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
     * Ends the loop and returns to the previous scope.
     *
     * @return Previous scope of the orchestration definition
     */
    public R endLoop() {
      return ForLoopCallScenarioConsumerDefinition.this.endLoop();
    }
  }
}
