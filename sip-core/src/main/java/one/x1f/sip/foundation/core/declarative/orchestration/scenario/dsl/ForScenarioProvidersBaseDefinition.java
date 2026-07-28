package one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Delegate;
import one.x1f.sip.foundation.core.declarative.orchestration.process.routebuilding.ScenarioStepIterations;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationContext;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/** DSL base class for specifying which consumers should be called for a scenario provider. */
public abstract sealed class ForScenarioProvidersBaseDefinition<
        S extends ForScenarioProvidersBaseDefinition<S, R, M>, R, M>
    extends ScenarioDslDefinitionBase<S, R, M> implements ScenarioConsumerCalls<S, R, M>
    permits ForScenarioProvidersCatchAllDefinition,
        ForScenarioProvidersByClassDefinition,
        ForScenarioProvidersByConnectorIdDefinition {

  @Getter(AccessLevel.PACKAGE)
  private final List<CallableWithinProviderDefinition> nodes = new ArrayList<>();

  @Delegate
  @Getter(AccessLevel.PACKAGE)
  private final ScenarioConsumerCallsDelegate<S, R, M> consumerCallsDelegate =
      new ScenarioConsumerCallsDelegate<>(
          nodes, self(), getDslReturnDefinition(), getIntegrationScenario());

  ForScenarioProvidersBaseDefinition(
      final R dslReturnDefinition, final IntegrationScenarioDefinition integrationScenario) {
    super(dslReturnDefinition, integrationScenario);
  }

  /**
   * Attaches and changes scope to a conditional branch that is only executed if the given <code>
   * predicate</code> matches at runtime for an integration call of the underlying scenario.
   *
   * <p>This can be compared to a Java <code>if</code>-statement, and the returned branch allows to
   * attach additional (conditional) branches similar to <code>else if</code> and <code>else</code>.
   *
   * <p>To leave the conditional statement and return to the current scope, use {@link
   * ConditionalCallScenarioConsumerDefinition.Branch#endCases()}.
   *
   * @see ConditionalCallScenarioConsumerDefinition.Branch
   * @see ConditionalCallScenarioConsumerDefinition.Branch#elseIfCase(Predicate)
   * @see ConditionalCallScenarioConsumerDefinition.Branch#elseCase()
   * @param predicate Predicate to test for execution of branch statmeents
   * @return The conditional branch
   */
  public ConditionalCallScenarioConsumerDefinition<S, M>.Branch<
          ConditionalCallScenarioConsumerDefinition<S, M>>
      ifCase(final Predicate<ScenarioOrchestrationContext<M>> predicate) {
    final var def =
        new ConditionalCallScenarioConsumerDefinition<S, M>(self(), getIntegrationScenario());
    nodes.add(def);
    return def.elseIfCase(predicate);
  }

  /**
   * Attaches and changes scope to a do while branch that will loop executions of the underlying
   * scenario until the given <code>predicate</code> no longer matches at runtime.
   *
   * <p>This can be compared to a Java <code>while</code>-statement.
   *
   * <p>To leave the do while statement and return to the current scope, use {@link
   * WhileLoopCallScenarioConsumerDefinition.Branch#endDoWhile()}.
   *
   * @param predicate Predicate to test for execution of branch statements
   * @return The do while branch
   */
  public WhileLoopCallScenarioConsumerDefinition<S, M>.Branch<
          WhileLoopCallScenarioConsumerDefinition<S, M>>
      doWhile(final Predicate<ScenarioOrchestrationContext<M>> predicate) {
    final var def =
        new WhileLoopCallScenarioConsumerDefinition<S, M>(self(), getIntegrationScenario());
    nodes.add(def);
    return def.doWhile(predicate);
  }

  /**
   * Attaches and changes scope to a for loop branch that will loop executions of the underlying
   * scenario for the provided amount of time.
   *
   * <p>This can be compared to a Java <code>for</code>-statement.
   *
   * <p>To leave the for loop statement and return to the current scope, use {@link
   * ForLoopCallScenarioConsumerDefinition.Branch#endForLoop()}.
   *
   * @param predicate Predicate to determine number of loop iterations
   * @return The for loop branch
   */
  public ForLoopCallScenarioConsumerDefinition<S, M>.Branch<
          ForLoopCallScenarioConsumerDefinition<S, M>>
      forLoop(final ScenarioStepIterations<M> predicate) {
    final var def =
        new ForLoopCallScenarioConsumerDefinition<S, M>(self(), getIntegrationScenario());
    nodes.add(def);
    return def.forLoop(predicate);
  }

  /**
   * Specifies that any scenario consumer (which includes outbound connectors) that is attached to
   * the integration scenario but not explicitly defined above will be called.
   *
   * <p>This is a terminal operation for the consumer call specifications, so it needs to be the
   * last call in the list and no additional consumers calls can be specified afterward.
   *
   * @return DSL handle for further call instructions
   */
  public CallScenarioConsumerCatchAllDefinition<R, M> callAnyUnspecifiedScenarioConsumer() {
    final CallScenarioConsumerCatchAllDefinition<R, M> def =
        new CallScenarioConsumerCatchAllDefinition<>(
            getDslReturnDefinition(), getIntegrationScenario());
    nodes.add(def);
    return def;
  }

  /**
   * Ends the orchestration-definitions for this provider and returns to the previous scope
   *
   * @return Previous scope
   */
  public R endDefinitionForThisProvider() {
    return getDslReturnDefinition();
  }

  public S process(final Consumer<ScenarioOrchestrationContext<M>> consumer) {
    ProcessCallScenarioConsumerDefinition<S, M> def = new ProcessCallScenarioConsumerDefinition<>(self(), getIntegrationScenario());
    nodes.add(def);
    return def.process(consumer);
  }
}
