package one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.impl;

import static java.lang.System.currentTimeMillis;
import static one.x1f.sip.foundation.testkit.util.TestKitHelper.*;
import static org.apache.camel.Exchange.*;
import static org.apache.camel.component.kafka.KafkaConstants.*;
import static org.apache.camel.component.kafka.KafkaConstants.OFFSET;

import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.RouteInvoker;
import one.x1f.sip.foundation.testkit.workflow.whenphase.routeinvoker.exceptions.RouteInvokerException;
import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.kafka.KafkaComponent;
import org.apache.camel.component.kafka.KafkaConfiguration;
import org.apache.camel.component.kafka.KafkaConsumer;
import org.apache.camel.component.kafka.KafkaEndpoint;
import org.apache.camel.component.kafka.serde.DefaultKafkaHeaderSerializer;
import org.apache.camel.component.kafka.serde.KafkaHeaderSerializer;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/** Invoker class for triggering routes with Kafka consumer */
@Component
@ConditionalOnClass(KafkaComponent.class)
@RequiredArgsConstructor
@Slf4j
public class KafkaRouteInvoker implements RouteInvoker {

  private final CamelContext camelContext;
  private static final List<String> kafkaSpecificHeaderKeys =
      List.of(
          PARTITION_KEY,
          PARTITION,
          KEY,
          TOPIC,
          OVERRIDE_TOPIC,
          OFFSET,
          HEADERS,
          LAST_RECORD_BEFORE_COMMIT,
          LAST_POLL_RECORD,
          TIMESTAMP,
          OVERRIDE_TIMESTAMP);

  @Override
  public Optional<Exchange> invoke(Exchange inputExchange) {
    KafkaConsumer kafkaConsumer = (KafkaConsumer) resolveConsumer(inputExchange, camelContext);

    Exchange kafkaExchange = kafkaConsumer.createExchange(false);
    kafkaExchange.getMessage().setBody(inputExchange.getMessage().getBody());
    kafkaExchange
        .getMessage()
        .setHeaders(
            createExchangeHeaders(
                inputExchange.getMessage().getHeaders(),
                kafkaConsumer.getEndpoint().getConfiguration()));

    try {
      kafkaConsumer.getProcessor().process(kafkaExchange);
    } catch (Exception e) {
      throw new RouteInvokerException(this.getClass().getName());
    }

    return Optional.empty();
  }

  @Override
  public boolean isApplicable(Endpoint endpoint) {
    return endpoint instanceof KafkaEndpoint;
  }

  @Override
  public boolean shouldSuspend(Endpoint endpoint) {
    return true;
  }

  private Map<String, Object> createExchangeHeaders(
      Map<String, Object> inputHeaders, KafkaConfiguration configuration) {
    Map<String, Object> kafkaHeaders =
        prepareKafkaSpecificHeaders(inputHeaders, configuration.getTopic());
    kafkaHeaders.putAll(prepareCustomHeaders(inputHeaders));
    return kafkaHeaders;
  }

  private Map<String, Object> prepareKafkaSpecificHeaders(
      Map<String, Object> inputHeaders, String kafkaTopic) {
    Map<String, Object> kafkaSpecificHeaders =
        inputHeaders.entrySet().stream()
            .filter(header -> isKafkaSpecificHeader(header.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    kafkaSpecificHeaders.putIfAbsent(TOPIC, kafkaTopic);
    kafkaSpecificHeaders.putIfAbsent(TIMESTAMP, currentTimeMillis());
    kafkaSpecificHeaders.putIfAbsent(MESSAGE_TIMESTAMP, kafkaSpecificHeaders.get(TIMESTAMP));
    return kafkaSpecificHeaders;
  }

  private Map<String, Object> prepareCustomHeaders(Map<String, Object> inputHeaders) {
    Map<String, Object> customHeaders =
        inputHeaders.entrySet().stream()
            .filter(header -> !isKafkaSpecificHeader(header.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    return serializeValuesAndAddKafkaSpecificHeader(customHeaders);
  }

  private boolean isKafkaSpecificHeader(String key) {
    return kafkaSpecificHeaderKeys.contains(key);
  }

  private Map<String, Object> serializeValuesAndAddKafkaSpecificHeader(
      Map<String, Object> customHeaders) {
    Map<String, Object> customHeadersResult = new HashMap<>();
    RecordHeaders recordHeaders = new RecordHeaders(new ArrayList<>());
    KafkaHeaderSerializer kafkaHeaderSerializer = new DefaultKafkaHeaderSerializer();

    customHeaders.forEach(
        (key, value) -> {
          if (!isTestKitHeader(key)) {
            byte[] serializedValue = kafkaHeaderSerializer.serialize(key, value);
            recordHeaders.add(key, serializedValue);
            customHeadersResult.put(key, serializedValue);
          } else {
            customHeadersResult.put(key, value);
          }
        });

    customHeadersResult.put(HEADERS, recordHeaders);
    return customHeadersResult;
  }
}
