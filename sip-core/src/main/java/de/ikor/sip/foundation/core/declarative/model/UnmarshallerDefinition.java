package de.ikor.sip.foundation.core.declarative.model;

import static de.ikor.sip.foundation.core.proxies.ProcessorProxy.TEST_MODE_HEADER;

import java.util.function.Consumer;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.spi.DataFormat;

/** Class providing various way to define an unmarshaller */
public interface UnmarshallerDefinition extends RouteDefinitionConsumer {

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
            .when(
                exchange ->
                    Boolean.parseBoolean(exchange.getProperty(TEST_MODE_HEADER, String.class)))
            .log("Skip unmarshal in test mode")
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
            .when(
                exchange ->
                    Boolean.parseBoolean(exchange.getProperty(TEST_MODE_HEADER, String.class)))
            .log("Skip unmarshal in test mode")
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
              .when(
                  exchange ->
                      Boolean.parseBoolean(exchange.getProperty(TEST_MODE_HEADER, String.class)))
              .log("Skip unmarshal in test mode")
              .otherwise();

      consumer.accept(choiceRoute.unmarshal());

      choiceRoute.endChoice();
    };
  }
}
