package one.x1f.sip.foundation.core.declarative.model;

import java.util.function.Consumer;
import org.apache.camel.builder.DataFormatClause;
import org.apache.camel.model.DataFormatDefinition;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.DataFormat;

/** Class providing various way to define a marshaller */
public interface MarshallerDefinition extends RouteDefinitionConsumer {

  /**
   * Creates a marshaller definition from a {@link DataFormat} instance
   *
   * @param dataFormat The data format
   * @return The marshaller definition
   */
  static MarshallerDefinition forDataFormat(final DataFormat dataFormat) {
    return routeBuilder -> routeBuilder.marshal(dataFormat);
  }

  /**
   * Creates a marshaller definition from a {@link DataFormatDefinition} instance
   *
   * @param dataFormatDefinition The data format definition
   * @return The marshaller definition
   */
  static MarshallerDefinition forDataFormat(final DataFormatDefinition dataFormatDefinition) {
    return routeBuilder -> routeBuilder.marshal(dataFormatDefinition);
  }

  /**
   * Creates a marshaller using a consumer for the fluent {@link DataFormatClause} API
   *
   * @param consumer Consumer for fluent API
   * @return The marshaller definition
   */
  static MarshallerDefinition forClause(
      final Consumer<DataFormatClause<ProcessorDefinition<RouteDefinition>>> consumer) {
    return routeBuilder -> consumer.accept(routeBuilder.marshal());
  }
}
