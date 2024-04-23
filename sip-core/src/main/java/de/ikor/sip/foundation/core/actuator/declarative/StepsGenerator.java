package de.ikor.sip.foundation.core.actuator.declarative;

import de.ikor.sip.foundation.core.actuator.declarative.model.dto.StepDto;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.orchestration.process.dsl.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;

/** Generator for steps of a {@link ProcessOrchestrationDefinition} */
@RequiredArgsConstructor
public class StepsGenerator {
  private int stepOrder = 0;
  private final ProcessOrchestrationDefinition orchestrationDef;

  /**
   * Generate {@link StepDto}s of the {@link ProcessOrchestrationDefinition}
   *
   * @return list of {@link StepDto}s
   */
  public List<StepDto> generateSteps() {
    return RouteGeneratorInternalHelper.getSteps(orchestrationDef).stream()
        .flatMap(this::createStepsFromDefinition)
        .toList();
  }

  private Stream<StepDto> createStepsFromDefinition(
      CallableWithinProcessDefinition callableWithinProcessDefinition) {
    List<StepDto> steps = new ArrayList<>();
    if (callableWithinProcessDefinition instanceof CallNestedCondition<?> nestedCondition) {
      fillConditionalSteps(
          steps, RouteGeneratorInternalHelper.getConditionalStatements(nestedCondition));
      fillUnconditionalSteps(
          steps, RouteGeneratorInternalHelper.getUnconditionalStatements(nestedCondition));
    }
    if (callableWithinProcessDefinition instanceof CallProcessConsumer<?, ?>) {
      steps.add(createBaseStep(callableWithinProcessDefinition));
    }
    if (callableWithinProcessDefinition
        instanceof CallWhileLoopStatement<?> callWhileLoopStatement) {
      fillWhileLoopSteps(
          steps, RouteGeneratorInternalHelper.getLoopProcess(callWhileLoopStatement));
    }
    if (callableWithinProcessDefinition instanceof CallForLoopStatement<?> callForLoopStatement) {
      fillForLoopSteps(steps, RouteGeneratorInternalHelper.getForLoopProcess(callForLoopStatement));
    }
    return steps.stream();
  }

  private void fillConditionalSteps(
      List<StepDto> steps,
      List<CallNestedCondition.ProcessBranchStatements> conditionalStatements) {
    conditionalStatements.forEach(
        conditionalStatement -> fillUnconditionalSteps(steps, conditionalStatement.statements()));
  }

  private void fillUnconditionalSteps(
      List<StepDto> steps, List<CallableWithinProcessDefinition> unconditionalStatements) {
    if (unconditionalStatements == null || unconditionalStatements.isEmpty()) return;
    List<StepDto> unconditionalNestedSteps = new ArrayList<>();
    unconditionalStatements.forEach(
        statement -> unconditionalNestedSteps.add(createBaseStep(statement)));
    steps.add(StepDto.builder().conditioned(true).nested(unconditionalNestedSteps).build());
  }

  private void fillWhileLoopSteps(
      List<StepDto> steps,
      List<CallWhileLoopStatement.ProcessBranchStatements> conditionalStatements) {
    conditionalStatements.forEach(
        conditionalStatement -> fillUnconditionalSteps(steps, conditionalStatement.statements()));
  }

  private void fillForLoopSteps(
      List<StepDto> steps,
      List<CallForLoopStatement.ProcessBranchStatements> conditionalStatements) {
    conditionalStatements.forEach(
        conditionalStatement -> fillUnconditionalSteps(steps, conditionalStatement.statements()));
  }

  private StepDto createBaseStep(CallableWithinProcessDefinition statement) {
    if (statement instanceof CallProcessConsumer<?, ?> base) {
      String consumer =
          RouteGeneratorInternalHelper.getConsumerClass(base)
              .getAnnotation(IntegrationScenario.class)
              .scenarioId();
      return StepDto.builder()
          .stepOrder(stepOrder++)
          .conditioned(false)
          .consumerId(consumer)
          .requestPreparation(RouteGeneratorInternalHelper.getRequestPreparation(base).isPresent())
          .responseHandling(RouteGeneratorInternalHelper.getResponseConsumer(base).isPresent())
          .build();
    }
    return null;
  }
}
