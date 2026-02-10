package one.x1f.sip.foundation.core.declarative.model;

import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;
import static one.x1f.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_PREDICATE;

import java.util.function.Consumer;
import java.util.function.Predicate;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.spi.DataFormat;

/** Class providing various way to define an unmarshaller */
public interface UnmarshallerDefinition extends RouteDefinitionConsumer {

  String SKIP_UNMARSHAL_MESSAGE = "Skip unmarshal in test mode";

  /**
   * Creates an unmarshaller definition from a {@link DataFormat} instance
   *
   * @param dataFormat The data format
   * @return The unmarshaller definition
   */
  static UnmarshallerDefinition forDataFormat(final DataFormat dataFormat) {
    return routeBuilder ->
        routeBuilder
            .choice()
            .when(shouldSkipUnmarshalInTestMode())
            .log(SKIP_UNMARSHAL_MESSAGE)
            .otherwise()
            .unmarshal(dataFormat)
            .endChoice();
  }

  /**
   * Creates an unmarshaller definition from a {@link DataFormatDefinition} instance
   *
   * @param dataFormatDefinition The data format definition
   * @return The unmarshaller definition
   */
  static UnmarshallerDefinition forDataFormat(final DataFormatDefinition dataFormatDefinition) {
    return routeBuilder ->
        routeBuilder
            .choice()
            .when(shouldSkipUnmarshalInTestMode())
            .log(SKIP_UNMARSHAL_MESSAGE)
            .otherwise()
            .unmarshal(dataFormatDefinition)
            .endChoice();
  }

  /**
   * Creates an unmarshaller using a consumer for the fluent {@link DataFormatClause} API
   *
   * @param consumer Consumer for fluent API
   * @return The unmarshaller definition
   */
  static UnmarshallerDefinition forClause(final Consumer<DataFormatClause<?>> consumer) {
    return routeBuilder -> {
      var choiceRoute =
          routeBuilder
              .choice()
              .when(shouldSkipUnmarshalInTestMode())
              .log(SKIP_UNMARSHAL_MESSAGE)
              .otherwise();

      consumer.accept(choiceRoute.unmarshal());

      choiceRoute.endChoice();
    };
  }

  private static org.apache.camel.Predicate shouldSkipUnmarshalInTestMode() {
    //noinspection unchecked
    return exchange ->
        exchange.getProperty(TEST_MODE_HEADER, boolean.class)
            && exchange.getProperty(TEST_MODE_PREDICATE, Predicate.class) != null
            && exchange.getProperty(TEST_MODE_PREDICATE, Predicate.class).test(exchange);
  }
}
