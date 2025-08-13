package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/** Class specifying consumer calls that are only executed conditionally */
public final class ConditionalCallScenarioConsumerDefinition<R, M>
    extends ScenarioDslDefinitionBase<ConditionalCallScenarioConsumerDefinition<R, M>, R, M>
    implements CallableWithinProviderDefinition {

  @Getter(AccessLevel.PACKAGE)
  private final List<BranchStatements<M>> conditionalStatements = new ArrayList<>();

  @Getter(AccessLevel.PACKAGE)
  private final List<CallableWithinProviderDefinition> unconditionalStatements = new ArrayList<>();

  ConditionalCallScenarioConsumerDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }

  /**
   * @see Branch#elseIfCase(Predicate)
   */
  Branch<ConditionalCallScenarioConsumerDefinition<R, M>> elseIfCase(
      final Predicate<ScenarioOrchestrationContext<M>> predicate) {
    final var branch = new BranchStatements<>(predicate, new ArrayList<>());
    conditionalStatements.add(branch);
    return new Branch<>(branch.statements, self(), getIntegrationScenario());
  }

  /**
   * @see Branch#elseCase()
   */
  Branch<R> elseCase() {
    return new Branch<>(
        unconditionalStatements, getDslReturnDefinition(), getIntegrationScenario());
  }

  /**
   * @see Branch#endCases()
   */
  R endCases() {
    return getDslReturnDefinition();
  }

  record BranchStatements<M>(
      Predicate<ScenarioOrchestrationContext<M>> predicate,
      List<CallableWithinProviderDefinition> statements) {}

  /**
   * Class representing on branch of a conditional statement:
   *
   * <ul>
   *   <li>Allows to add consumer-calls to this conditional branch using the statements defined in
   *       {@link ScenarioConsumerCalls}
   *   <li>Can spawn alternative branches for the condition via {@link #elseIfCase(Predicate)} and
   *       {@link #elseCase()}
   * </ul>
   */
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
     * Defines an alternative conditional branch that is executed if the given <code>predicate
     * </code> matches.
     *
     * @param predicate Predicate to test for execution of the branch
     * @return The conditional branch
     */
    public Branch<ConditionalCallScenarioConsumerDefinition<R, M>> elseIfCase(
        final Predicate<ScenarioOrchestrationContext<M>> predicate) {
      return ConditionalCallScenarioConsumerDefinition.this.elseIfCase(predicate);
    }

    /**
     * Defines a non-conditional branch that is executed if none of the previous conditional ones
     * matched.
     *
     * <p>Needs to be the last statement of the condition - all conditional branches should be
     * attached previously to using this method via {@link #elseIfCase(Predicate)}.
     *
     * @return The unconditional branch
     */
    public Branch<R> elseCase() {
      return ConditionalCallScenarioConsumerDefinition.this.elseCase();
    }

    /**
     * Ends the condition and returns to the previous scope.
     *
     * @return Previous scope of the orchestration definition
     */
    public R endCases() {
      return ConditionalCallScenarioConsumerDefinition.this.endCases();
    }
  }
}
