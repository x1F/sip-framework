package de.ikor.sip.foundation.core.declarative.orchestration.process;

import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.process.dsl.ProcessOrchestrationDefinition;
import de.ikor.sip.foundation.core.declarative.orchestration.process.routebuilding.RouteGeneratorForProcessOrchestrationDefinition;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Orchestrator meant to be attached to {@link
 * de.ikor.sip.foundation.core.declarative.annonation.CompositeProcess}
 *
 * <p>Orchestration can be defined in orchestration DSL via {@link #forOrchestrationDsl(Consumer)}.
 *
 * @see CompositeProcessDefinition#getOrchestrator()
 */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessOrchestrator implements Orchestrator<CompositeProcessOrchestrationInfo> {

  private final Consumer<CompositeProcessOrchestrationInfo> orchestrationInfoConsumer;
  @Setter private Predicate<CompositeProcessOrchestrationInfo> canOrchestrate = Objects::nonNull;

  private Optional<Consumer<ProcessOrchestrationDefinition>> dslDefinition = Optional.empty();

  private ProcessOrchestrator(
      Consumer<CompositeProcessOrchestrationInfo> orchestrationInfoConsumer,
      Consumer<ProcessOrchestrationDefinition> dslDefinition) {
    this.orchestrationInfoConsumer = orchestrationInfoConsumer;
    this.dslDefinition = Optional.of(dslDefinition);
  }

  /**
   * Creates a new orchestrator specified via orchestration-DSL
   *
   * @param dslDefinition Consumer that specifies the orchestration via DSL
   * @return Orchestrator as specified in the DSL
   */
  @SuppressWarnings("java:S1172")
  public static ProcessOrchestrator forOrchestrationDsl(
      final Consumer<ProcessOrchestrationDefinition> dslDefinition) {
    return new ProcessOrchestrator(
        orchestrationInfo -> {
          final var orchestrationDef =
              new ProcessOrchestrationDefinition(orchestrationInfo.getCompositeProcess());
          dslDefinition.accept(orchestrationDef);
          new RouteGeneratorForProcessOrchestrationDefinition(orchestrationInfo, orchestrationDef)
              .run();
        },
        dslDefinition);
  }

  /**
   * Creates a new orchestrator specified via a consumer for the {@link
   * CompositeProcessOrchestrationInfo}.
   *
   * <p>This is very low-level, and it is strongly recommended to define the orchestration via DSL
   * instead. This can be used to manually create routes between endpoints.
   *
   * @param orchestrationInfoConsumer Consumer for the orchestration-info provided by the framework
   * @return Orchestrator
   */
  public static ProcessOrchestrator forOrchestrationConsumer(
      final Consumer<CompositeProcessOrchestrationInfo> orchestrationInfoConsumer) {
    return new ProcessOrchestrator(orchestrationInfoConsumer);
  }

  /**
   * Provides a way to get the definition that is created inside the DSL.
   *
   * @param compositeProcessDefinition definition of a composite process
   * @return orchestration definition
   */
  public ProcessOrchestrationDefinition populateOrchestrationDefinition(
      CompositeProcessDefinition compositeProcessDefinition) {
    if (dslDefinition.isPresent()) {
      final var orchestrationDef = new ProcessOrchestrationDefinition(compositeProcessDefinition);
      dslDefinition.get().accept(orchestrationDef);
      return orchestrationDef;
    }
    return null;
  }

  @Override
  public boolean canOrchestrate(final CompositeProcessOrchestrationInfo info) {
    return canOrchestrate.test(info);
  }

  @Override
  public void doOrchestrate(final CompositeProcessOrchestrationInfo info) {
    orchestrationInfoConsumer.accept(info);
  }
}
