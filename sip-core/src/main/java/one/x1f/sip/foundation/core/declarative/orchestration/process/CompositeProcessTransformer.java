package one.x1f.sip.foundation.core.declarative.orchestration.process;

/** Interface to expose orchestration context in process step */
@FunctionalInterface
public interface CompositeProcessTransformer {

  /**
   * Define processing on orchestration context
   *
   * @param context The current orchestration context
   */
  void process(final CompositeProcessOrchestrationContext context);
}
