package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;

/**
 * DSL class calling a consumer by its {@link IntegrationScenarioDefinition} class
 *
 * @param <S> DSL handle for caller
 * @param <R> DSL handle for the return DSL Verb/type.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class ForProcessProvidersDelegate<S extends ProcessConsumerCalls<S, R>, R>
    implements ProcessConsumerCalls<S, R> {

  @Getter(AccessLevel.PACKAGE)
  private final List<CallableWithinProcessDefinition> consumerCalls;

  @Getter(AccessLevel.PACKAGE)
  private final S callerNode;

  @Getter(AccessLevel.PACKAGE)
  private final R returningNode;

  /**
   * Define a consumer of the request from process orchestration
   *
   * @param consumerClass class which will consume the request
   * @return DSL to prepare request or handle response
   */
  @Override
  public CallProcessConsumer<? extends CallProcessConsumer<?, ?>, S> callConsumer(
      Class<? extends IntegrationScenarioDefinition> consumerClass) {
    final CallProcessConsumer<? extends CallProcessConsumer<?, ?>, S> def =
        new CallProcessConsumer<>(callerNode, null, consumerClass);
    consumerCalls.add(def);
    return def;
  }
}
