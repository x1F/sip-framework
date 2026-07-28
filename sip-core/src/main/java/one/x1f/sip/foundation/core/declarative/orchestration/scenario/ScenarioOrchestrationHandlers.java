package one.x1f.sip.foundation.core.declarative.orchestration.scenario;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.core.declarative.orchestration.common.dsl.StepResultCloner;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl.ScenarioStepRequestExtractor;
import one.x1f.sip.foundation.core.declarative.orchestration.scenario.dsl.ScenarioStepResponseConsumer;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import one.x1f.sip.foundation.core.util.exception.SIPFrameworkException;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;

/**
 * Various handlers use in scenario orchestration *
 *
 * <p><em>For internal use only</em>
 */
@UtilityClass
@Slf4j
public class ScenarioOrchestrationHandlers {

  private final String CALLED_CONSUMER_LIST_PROPERTY = "_SipCalledConsumersList";

  public static ContextInitializer handleContextInitialization(
      final IntegrationScenarioDefinition scenario) {
    return new ContextInitializer(scenario);
  }

  public static <M> ConsumerRequestHandler<M> handleRequestToConsumer(
      final IntegrationScenarioConsumerDefinition consumerDefinition,
      final Optional<ScenarioStepRequestExtractor<M>> requestPreparation) {
    return new ConsumerRequestHandler<>(consumerDefinition, requestPreparation);
  }

  public static <M> ConsumerResponseHandler<M> handleResponseFromConsumer(
      final IntegrationScenarioConsumerDefinition consumer,
      final Optional<StepResultCloner<M>> stepResultCloner,
      final Optional<ScenarioStepResponseConsumer<M>> responseConsumer) {
    return new ConsumerResponseHandler<>(consumer, stepResultCloner, responseConsumer);
  }

  public static <M> ContextPredicateHandler<M> handleContextPredicate(
      final Predicate<ScenarioOrchestrationContext<M>> predicate) {
    return new ContextPredicateHandler<>(predicate);
  }

  public static <M> ContextConsumerHandler<M> handleContextConsumer(
          final Consumer<ScenarioOrchestrationContext<M>> consumer) {
    return new ContextConsumerHandler<>(consumer);
  }

  public static ThrowErrorOnUnhandledRequestHandler handleErrorThrownIfNoConsumerWasCalled() {
    return new ThrowErrorOnUnhandledRequestHandler();
  }

  @SuppressWarnings("unchecked")
  private static <M> ScenarioOrchestrationContext<M> retrieveOrchestrationContext(
      final Exchange exchange) {
    final var context =
        Objects.requireNonNull(
            exchange.getProperty(
                ScenarioOrchestrationContext.PROPERTY_NAME, ScenarioOrchestrationContext.class),
            "Orchestration context for scenario-orchestration could not be retrieved from exchange");
    context.setExchange(exchange);
    return context;
  }

  @SuppressWarnings("unchecked")
  private static List<IntegrationScenarioConsumerDefinition> retrieveCalledConsumerList(
      final Exchange exchange) {
    return Objects.requireNonNull(
        exchange.<List>getProperty(CALLED_CONSUMER_LIST_PROPERTY, List.class),
        "Could not retrieve list of called consumers from Exchange");
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ContextInitializer {

    private final IntegrationScenarioDefinition integrationScenario;

    @Handler
    public <T> void initializeOrchestrationContext(final T body, final Exchange exchange) {
      exchange.setProperty(
          CALLED_CONSUMER_LIST_PROPERTY,
          Collections.synchronizedList(new ArrayList<IntegrationScenarioConsumerDefinition>()));
      exchange.setProperty(
          ScenarioOrchestrationContext.PROPERTY_NAME,
          ScenarioOrchestrationContext.builder()
              .integrationScenario(integrationScenario)
              .originalRequest(body)
              .exchange(exchange)
              .build());
    }
  }

  static class ConsumerRequestHandler<M> {
    private final IntegrationScenarioConsumerDefinition consumerDefinition;
    private final ScenarioStepRequestExtractor<M> requestPreparation;

    private ConsumerRequestHandler(
        final IntegrationScenarioConsumerDefinition consumerDefinition,
        final Optional<ScenarioStepRequestExtractor<M>> requestPreparation) {
      this.consumerDefinition = consumerDefinition;
      this.requestPreparation =
          requestPreparation.orElseGet(ConsumerRequestHandler::defaultRequestExtractor);
    }

    private static <M> ScenarioStepRequestExtractor<M> defaultRequestExtractor() {
      return ScenarioOrchestrationContext::getOriginalRequest;
    }

    @Handler
    public <T> Object extractRequest(final T body, final Exchange exchange) {
      retrieveCalledConsumerList(exchange).add(consumerDefinition);
      return requestPreparation.extractStepRequest(retrieveOrchestrationContext(exchange));
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ConsumerResponseHandler<M> {
    private final IntegrationScenarioConsumerDefinition consumer;
    private final Optional<StepResultCloner<M>> stepResultCloner;
    private final Optional<ScenarioStepResponseConsumer<M>> responseConsumer;

    @Handler
    public M handleResponse(final M body, final Exchange exchange) {
      final ScenarioOrchestrationContext<M> context = retrieveOrchestrationContext(exchange);
      context.addResponseForStep(consumer, body, stepResultCloner);
      responseConsumer.ifPresent(c -> c.consumeResponse(body, context));
      return context.getAggregatedResponse().orElse(body);
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ContextPredicateHandler<M> {
    private final Predicate<ScenarioOrchestrationContext<M>> predicate;

    @Handler
    public boolean testPredicate(final Exchange exchange) {
      final ScenarioOrchestrationContext<M> context = retrieveOrchestrationContext(exchange);
      return predicate.test(context);
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ContextConsumerHandler<M> {
    private final Consumer<ScenarioOrchestrationContext<M>> consumer;

    @Handler
    public void testPredicate(final Exchange exchange) {
      final ScenarioOrchestrationContext<M> context = retrieveOrchestrationContext(exchange);
      consumer.accept(context);
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  static class ThrowErrorOnUnhandledRequestHandler {
    @Handler
    public void checkHandled(final Exchange exchange) {
      if (retrieveCalledConsumerList(exchange).isEmpty()) {
        final var context = retrieveOrchestrationContext(exchange);
        throw SIPFrameworkException.init(
            "No integration-scenario consumer was called during orchestration of integration-scenario '%s'. The orchestration-definition should be modified so that at least one consumer always reacts to a request.",
            context.getIntegrationScenario().getId());
      }
    }
  }
}
