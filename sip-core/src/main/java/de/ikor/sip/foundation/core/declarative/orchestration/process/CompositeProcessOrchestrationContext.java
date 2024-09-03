package de.ikor.sip.foundation.core.declarative.orchestration.process;

import de.ikor.sip.foundation.core.declarative.orchestration.common.dsl.StepResultCloner;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessDefinition;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioDefinition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.*;
import org.apache.camel.Exchange;

/**
 * Context used during the orchestration of a composite process.
 *
 * <p>Holds the original request from the provider that initiated the orchestration, as well as a
 * list of requests and responses received from the consumers called during the orchestration.
 *
 * <p>It also supports storing an aggregated response for the integration-call, which can be created
 * during the orchestration-run by integrating consumer-responses via {@link
 * #setProcessResponse(Object, Optional)}. Aggregated response can be used as a temporary object
 * that is filled through the orchestration.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SuppressWarnings("rawtypes")
public class CompositeProcessOrchestrationContext {

  /**
   * Record that holds the response for a single orchestration step
   *
   * @param consumer Consumer that was called
   * @param request Request that was sent to the consumer
   * @param response Response as returned by that consumer
   */
  public record OrchestrationStep(
      IntegrationScenarioDefinition consumer, Object request, Object response) {}

  /** Key that is used inside Camel's exchange ot save the context. */
  public static final String CAMEL_PROPERTY_NAME = "SipComplexScenarioOrchestrationContext";

  @Getter private final CompositeProcessDefinition compositeProcess;
  private final Object originalRequest;
  private final List<OrchestrationStep> orchestrationSteps =
      Collections.synchronizedList(new ArrayList<>());

  private Object processResponse;

  /** Raw underlying Camel exchange. To be used with care! */
  @Getter
  @Setter(AccessLevel.PACKAGE)
  private Exchange exchange;

  /**
   * Returns the original request as retrieved from the provider that initiated the integration call
   * with the given type.
   *
   * @param requestType Request model class
   * @return Original request
   * @param <T> Type of the request-model
   */
  public <T> T getOriginalRequest(final Class<T> requestType) {
    return getOriginalRequest();
  }

  /**
   * Returns the original request as retrieved from the provider that initiated the integration
   * call.
   *
   * @return Original request
   * @param <T> Type of the request-model
   */
  @SuppressWarnings("unchecked")
  public <T> T getOriginalRequest() {
    return (T) originalRequest;
  }

  /**
   * Returns the last response payload (if exists) with the given type
   *
   * @param responseType Response model class
   * @return last response
   * @param <T> Type of the response model
   */
  @Synchronized
  public <T> Optional<T> getLatestResponse(final Class<T> responseType) {
    return getLatestResponse();
  }

  /**
   * Returns the last response payload (if exists)
   *
   * @return last response
   * @param <T> Type of the response model
   */
  @Synchronized
  @SuppressWarnings("unchecked")
  public <T> Optional<T> getLatestResponse() {
    return (Optional<T>) getResultForLatestStep().map(OrchestrationStep::response);
  }

  /**
   * Returns the response of the process (if exists)
   *
   * @return process response
   * @see #setProcessResponse(Object, Optional)
   */
  @Synchronized
  public Optional<Object> getProcessResponse() {
    return Optional.ofNullable(processResponse);
  }

  /**
   * Sets the overall process response for the integration call. If the response isn't set then the
   * last response is automatically returned as a process response.
   *
   * @param response response that should be saved
   * @param cloner optional cloner for the response object
   * @return the aggregated response
   * @see #getProcessResponse()
   */
  @Synchronized
  public Object setProcessResponse(
      final Object response, final Optional<StepResultCloner<Object>> cloner) {
    processResponse = cloner.map(c -> c.apply(response)).orElse(response);
    return processResponse;
  }

  /**
   * Returns the response from the latest called consumer (if exists)
   *
   * @return optional response from latest consumer call
   */
  @Synchronized
  public Optional<OrchestrationStep> getResultForLatestStep() {
    return orchestrationSteps.isEmpty()
        ? Optional.empty()
        : Optional.of(orchestrationSteps.get(orchestrationSteps.size() - 1));
  }

  /**
   * Stores a response from the call to a consumer.
   *
   * <p><em>Internal use only</em>
   *
   * @param consumer Consumer that provided the response
   * @param response Response as it was received from the consumer
   * @param cloner Optional cloner for the response
   */
  @Synchronized
  void addResponseForStep(
      final IntegrationScenarioDefinition consumer,
      final Object response,
      final Optional<StepResultCloner<Object>> cloner) {
    final Object maybeClonedResponse = cloner.map(c -> c.apply(response)).orElse(response);
    getResultOfLastStepFromConsumer(consumer.getClass())
        .ifPresent(
            step -> {
              OrchestrationStep lastStep =
                  orchestrationSteps.remove(orchestrationSteps.indexOf(step));
              orchestrationSteps.add(
                  new OrchestrationStep(consumer, lastStep.request, maybeClonedResponse));
            });
  }

  /**
   * Stores a request from the call to a consumer.
   *
   * <p><em>Internal use only</em>
   *
   * @param consumer Consumer that provided the response
   * @param request Request as it was sent to the consumer
   * @param cloner Optional cloner for the response
   */
  @Synchronized
  void addRequestForStep(
      final IntegrationScenarioDefinition consumer,
      final Object request,
      final Optional<StepResultCloner<Object>> cloner) {
    final Object maybeClonedRequest = cloner.map(c -> c.apply(request)).orElse(request);
    orchestrationSteps.add(new OrchestrationStep(consumer, maybeClonedRequest, null));
  }

  /**
   * @return Unmodifiable List of responses received from consumers so far
   */
  public List<OrchestrationStep> getOrchestrationSteps() {
    return Collections.unmodifiableList(orchestrationSteps);
  }

  /**
   * Retrieves the <em>first</em> result returned by the consumer specified by its {@code
   * consumerClass}. The result holds both request and response objects.
   *
   * @param consumerClass Class of the consumer for which to retrieve consumer
   * @return Optional first response of this consumer
   */
  public Optional<OrchestrationStep> getResultOfFirstStepFromConsumer(
      final Class<? extends IntegrationScenarioDefinition> consumerClass) {
    return getOrchestrationSteps().stream()
        .filter(step -> consumerClass.isInstance(step.consumer()))
        .findFirst();
  }

  /**
   * Retrieves the <em>last</em> response returned by the consumer specified by its {@code
   * consumerClass}. The result holds both request and response objects.
   *
   * @param consumerClass Class of the consumer for which to retrieve consumer
   * @return Optional last response of this consumer
   */
  public Optional<OrchestrationStep> getResultOfLastStepFromConsumer(
      final Class<? extends IntegrationScenarioDefinition> consumerClass) {
    return getOrchestrationSteps().stream()
        .filter(step -> consumerClass.isInstance(step.consumer()))
        .reduce((first, second) -> second);
  }

  /**
   * Returns the header with the given name and type, if it exists. It is a Camel's low level header
   * and should be used with care.
   *
   * @param name Name of the header to retrieve
   * @param type Header type class
   * @return Optional containing the header if it exists
   * @param <T> Header type
   * @see org.apache.camel.Message#getHeader(String, Class)
   */
  public <T> Optional<T> getHeader(final String name, final Class<T> type) {
    return Optional.ofNullable(getExchange().getMessage().getHeader(name, type));
  }
}
