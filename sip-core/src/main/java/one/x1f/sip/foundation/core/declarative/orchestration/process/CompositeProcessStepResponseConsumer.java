package one.x1f.sip.foundation.core.declarative.orchestration.process;

/** Interface to consume or handle the response of an individual process consumer call */
@FunctionalInterface
public interface CompositeProcessStepResponseConsumer {

  /**
   * Consumes or handles the response of an individual consumer call.
   *
   * @param latestResponse Response received from the process consumer
   * @param context Current orchestration context
   */
  void consumeResponse(Object latestResponse, CompositeProcessOrchestrationContext context);
}
