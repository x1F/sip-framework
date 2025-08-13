package one.x1f.sip.foundation.core.declarative.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.MarshalDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MarshallerDefinitionTest {

  RouteDefinition routeDefinition;

  @BeforeEach
  void setUp() {
    routeDefinition = new RouteDefinition();
  }

  @Test
  void WHEN_marshallerUsedWithDataFormatDefinition_THEN_marshallerIsAddedToRoute() {
    // arrange
    JaxbDataFormat dataFormatDefinition = new JaxbDataFormat();

    // act
    MarshallerDefinition marshallerDefinition =
        MarshallerDefinition.forDataFormat(dataFormatDefinition);
    marshallerDefinition.accept(routeDefinition);

    // assert
    assertThat(routeDefinition.getOutputs())
        .hasSize(1)
        .hasExactlyElementsOfTypes(MarshalDefinition.class);
    assertThat(((MarshalDefinition) routeDefinition.getOutputs().get(0)).getDataFormatType())
        .isInstanceOf(dataFormatDefinition.getClass());
  }

  @Test
  void WHEN_marshallerUsedWithDataFormat_THEN_marshallerIsAddedToRoute() {
    // arrange
    JacksonDataFormat jacksonDataFormat = new JacksonDataFormat();

    // act
    MarshallerDefinition marshallerDefinition =
        MarshallerDefinition.forDataFormat(jacksonDataFormat);
    marshallerDefinition.accept(routeDefinition);

    // assert
    assertThat(routeDefinition.getOutputs())
        .hasSize(1)
        .hasExactlyElementsOfTypes(MarshalDefinition.class);
    assertThat(
            ((MarshalDefinition) routeDefinition.getOutputs().get(0))
                .getDataFormatType()
                .getDataFormat())
        .isInstanceOf(jacksonDataFormat.getClass());
  }
}
