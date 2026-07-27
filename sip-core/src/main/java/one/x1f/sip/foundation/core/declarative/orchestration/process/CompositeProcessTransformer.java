package one.x1f.sip.foundation.core.declarative.orchestration.process;

/** Interface to extract the request object for a process consumer call */
@FunctionalInterface
public interface CompositeProcessTransformer {

  /**
   * Returns the request object to be used with the consumer call
   *
   * @param context The current orchestration context
   * @return Request sent to the process consumer
   */
  void process(final CompositeProcessOrchestrationContext context);
}
