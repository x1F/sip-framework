package de.ikor.sip.foundation.core.declarative.orchestration.process;

import de.ikor.sip.foundation.core.declarative.orchestration.common.dsl.StepResultCloner;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioConsumerDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import de.ikor.sip.foundation.core.util.exception.SIPFrameworkException;
import java.util.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.Handler;

/**
 * Various handlers use in process orchestration. Handlers will be a part of the generated routes.
 *
 * <p><em>For internal use only</em>
 */
@UtilityClass
@Slf4j
@SuppressWarnings("rawtypes")
public class CompositeProcessOrchestrationHandlers {

  private final String CALLED_CONSUMER_LIST_PROPERTY =
      "_SipProcessOrchestrationCalledConsumersList";

  public static ContextInitializer handleContextInitialization(
      final CompositeProcessDefinition scenario) {
    return new ContextInitializer(scenario);
  }

  public static ConsumerRequestHandler handleRequestToConsumer(
      final IntegrationScenarioDefinition consumerDefinition,
      final Optional<CompositeProcessStepRequestExtractor> requestPreparation) {
    return new ConsumerRequestHandler(consumerDefinition, requestPreparation);
  }

  public static ConsumerWithExpressionRequestHandler handleRequestToConsumerWithExpression(
      final IntegrationScenarioDefinition consumerDefinition,
      final Optional<CompositeProcessStepRequestExtractor> requestPreparation) {
    return new ConsumerWithExpressionRequestHandler(consumerDefinition, requestPreparation);
  }

  public static Boolean handleConditional(
      final Exchange exchange,
      final Optional<StepResultCloner> stepResultCloner,
      final Optional<CompositeProcessStepConditional> conditional) {
    return new ConditionalHandler(stepResultCloner, conditional).executeCondition(exchange);
  }

  public static int handleIterations(
      final Exchange exchange, final Optional<CompositeProcessStepIterations> iterations) {
    return new IterationsHandler(iterations).determineIterations(exchange);
  }

  public static List<?> handleSplitArray(
      final Exchange exchange, final Optional<CompositeProcessStepSplitExpression> expression) {
    return new ArrayHandler(expression).determineList(exchange);
  }

  public static ConsumerResponseHandler handleResponseFromConsumer(
      final IntegrationScenarioDefinition consumer,
      final Optional<StepResultCloner<Object>> stepResultCloner,
      final Optional<CompositeProcessStepResponseConsumer> responseConsumer) {
    return new ConsumerResponseHandler(consumer, stepResultCloner, responseConsumer);
  }

  public static ThrowErrorOnUnhandledRequestHandler handleErrorThrownIfNoConsumerWasCalled() {
    return new ThrowErrorOnUnhandledRequestHandler();
  }

  private static CompositeProcessOrchestrationContext retrieveOrchestrationContext(
      final Exchange exchange) {
    final var context =
        Objects.requireNonNull(
            exchange.getProperty(
                CompositeProcessOrchestrationContext.CAMEL_PROPERTY_NAME,
                CompositeProcessOrchestrationContext.class),
            "Orchestration context for scenario-orchestration could not be retrieved from exchange");
    context.setExchange(exchange);
    return context;
  }

  @SuppressWarnings("unchecked")
  private static List<IntegrationScenarioDefinition> retrieveCalledConsumerList(
      final Exchange exchange) {
    return Objects.requireNonNull(
        exchange.<List>getProperty(CALLED_CONSUMER_LIST_PROPERTY, List.class),
        "Orchestration Exception - Could not retrieve list of called consumers from Exchange");
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ContextInitializer {

    private final CompositeProcessDefinition compositeProcess;

    @Handler
    public <T> void initializeOrchestrationContext(final T body, final Exchange exchange) {
      exchange.setProperty(
          CALLED_CONSUMER_LIST_PROPERTY,
          Collections.synchronizedList(new ArrayList<IntegrationScenarioConsumerDefinition>()));
      exchange.setProperty(
          CompositeProcessOrchestrationContext.CAMEL_PROPERTY_NAME,
          CompositeProcessOrchestrationContext.builder()
              .compositeProcess(compositeProcess)
              .originalRequest(body)
              .exchange(exchange)
              .build());
    }
  }

  static class ConsumerRequestHandler {
    private final IntegrationScenarioDefinition consumerDefinition;
    private final CompositeProcessStepRequestExtractor requestPreparation;

    private ConsumerRequestHandler(
        final IntegrationScenarioDefinition consumerDefinition,
        final Optional<CompositeProcessStepRequestExtractor> requestPreparation) {
      this.consumerDefinition = consumerDefinition;
      this.requestPreparation =
          requestPreparation.orElseGet(ConsumerRequestHandler::defaultRequestExtractor);
    }

    private static CompositeProcessStepRequestExtractor defaultRequestExtractor() {
      return CompositeProcessOrchestrationContext::getOriginalRequest;
    }

    @Handler
    public <T> Object extractRequest(final T body, final Exchange exchange) {
      retrieveCalledConsumerList(exchange).add(consumerDefinition);
      final CompositeProcessOrchestrationContext context = retrieveOrchestrationContext(exchange);
      var request = requestPreparation.extractStepRequest(retrieveOrchestrationContext(exchange));
      context.addRequestForStep(consumerDefinition, request, Optional.empty());
      return request;
    }
  }

  static class ConsumerWithExpressionRequestHandler {
    private final IntegrationScenarioDefinition consumerDefinition;
    private final CompositeProcessStepRequestExtractor requestPreparation;

    private ConsumerWithExpressionRequestHandler(
        final IntegrationScenarioDefinition consumerDefinition,
        final Optional<CompositeProcessStepRequestExtractor> requestPreparation) {
      this.consumerDefinition = consumerDefinition;
      this.requestPreparation =
          requestPreparation.orElseGet(
              ConsumerWithExpressionRequestHandler::defaultRequestExtractor);
    }

    private static CompositeProcessStepRequestExtractor defaultRequestExtractor() {
      return context -> context.getExchange().getMessage().getBody();
    }

    @Handler
    public synchronized <T> Object extractRequest(final T body, final Exchange exchange) {
      retrieveCalledConsumerList(exchange).add(consumerDefinition);
      final CompositeProcessOrchestrationContext context = retrieveOrchestrationContext(exchange);
      var request = requestPreparation.extractStepRequest(retrieveOrchestrationContext(exchange));
      context.addRequestForStep(consumerDefinition, request, Optional.empty());
      return request;
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ConditionalHandler {
    private final Optional<StepResultCloner> stepResultCloner;
    private final Optional<CompositeProcessStepConditional> conditional;

    @Handler
    public boolean executeCondition(final Exchange exchange) {
      final CompositeProcessOrchestrationContext context = retrieveOrchestrationContext(exchange);
      if (conditional.isPresent()) {
        return conditional.get().determineCondition(context);
      }
      return true;
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class IterationsHandler {
    private final Optional<CompositeProcessStepIterations> iterations;

    @Handler
    public int determineIterations(final Exchange exchange) {
      final CompositeProcessOrchestrationContext context = retrieveOrchestrationContext(exchange);
      if (iterations.isPresent()) {
        return iterations.get().determineIterations(context);
      }
      return 0;
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ArrayHandler {
    private final Optional<CompositeProcessStepSplitExpression> expression;

    @Handler
    public List<?> determineList(final Exchange exchange) {
      final CompositeProcessOrchestrationContext context = retrieveOrchestrationContext(exchange);
      if (expression.isPresent()) {
        return expression.get().determinePayload(context);
      }
      return new ArrayList<>();
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  static class ConsumerResponseHandler {
    private final IntegrationScenarioDefinition consumer;
    private final Optional<StepResultCloner<Object>> stepResultCloner;
    private final Optional<CompositeProcessStepResponseConsumer> responseConsumer;

    @Handler
    public Object handleResponse(final Object body, final Exchange exchange) {
      final CompositeProcessOrchestrationContext context = retrieveOrchestrationContext(exchange);
      context.addResponseForStep(consumer, body, stepResultCloner);
      responseConsumer.ifPresent(c -> c.consumeResponse(body, context));
      return context.getProcessResponse().orElse(body);
    }
  }

  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  static class ThrowErrorOnUnhandledRequestHandler {
    @Handler
    public void checkHandled(final Exchange exchange) {
      if (retrieveCalledConsumerList(exchange).isEmpty()) {
        final var context = retrieveOrchestrationContext(exchange);
        throw SIPFrameworkException.init(
            "No consumer was called during orchestration of composite process '%s'. The orchestration-definition should be modified so that at least one consumer always reacts to a request.",
            context.getCompositeProcess().getId());
      }
    }
  }
}
