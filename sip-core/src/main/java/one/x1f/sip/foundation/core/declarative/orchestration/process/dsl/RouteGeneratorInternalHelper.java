package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.StepResultCloner;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessStepRequestExtractor;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessStepResponseConsumer;
import one.x1f.sip.foundation.core.declarative.orchestration.process.CompositeProcessTransformer;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/**
 * Class that exposes the insides of orchestration definition. Those are package private so that
 * they can't be seen on the user side (while the orchestration is written). This class has to stay
 * in the same package as the orchestration definition.
 *
 * <p><em>For internal use only</em>
 */
@UtilityClass
@SuppressWarnings({"rawtypes", "unchecked"})
public class RouteGeneratorInternalHelper {

  public static List<CallableWithinProcessDefinition> getConsumerCalls(
      ProcessOrchestrationDefinition element) {
    return element.getSteps();
  }

  public static Class<? extends IntegrationScenarioDefinition> getConsumerClass(
      CallProcessConsumer element) {
    return element.getConsumerClass();
  }

  public static Optional<CompositeProcessStepRequestExtractor> getRequestPreparation(
      CallProcessConsumer element) {
    return element.getRequestPreparation();
  }

  public static Optional<CompositeProcessStepResponseConsumer> getResponseConsumer(
      CallProcessConsumer element) {
    return element.getResponseConsumer();
  }

  public static Optional<CompositeProcessTransformer> getProcess(CallProcess element) {
    return element.getProcess();
  }

  public static List<CallNestedCondition.ProcessBranchStatements> getConditionalStatements(
      CallNestedCondition element) {
    return element.getConditionalStatements();
  }

  public static List<CallableWithinProcessDefinition> getUnconditionalStatements(
      CallNestedCondition element) {
    return element.getUnconditionalStatements();
  }

  public static Optional<StepResultCloner<Object>> getStepResultCloner(
      CallProcessConsumer element) {
    return element.getStepResultCloner();
  }

  public static Optional<StepResultCloner> getStepResultCloner(CallWhileLoopStatement element) {
    return element.getStepResultCloner();
  }

  public static Optional<StepResultCloner> getStepResultCloner(CallNestedCondition element) {
    return element.getStepResultCloner();
  }

  public static List<CallableWithinProcessDefinition> getSteps(
      ProcessOrchestrationDefinition orchestrationDef) {
    return orchestrationDef.getSteps();
  }

  public static List<CallWhileLoopStatement.ProcessBranchStatements> getLoopProcess(
      CallWhileLoopStatement element) {
    return element.getLoopProcess();
  }

  public static List<CallForLoopStatement.ProcessBranchStatements> getForLoopProcess(
      CallForLoopStatement element) {
    return element.getLoopProcess();
  }

  public static List<CallSplitStatement.ProcessBranchStatements> getSplitProcess(
      CallSplitStatement element) {
    return element.getSplitProcess();
  }
}
