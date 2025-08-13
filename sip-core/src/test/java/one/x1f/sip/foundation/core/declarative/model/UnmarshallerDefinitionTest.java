package one.x1f.sip.foundation.core.declarative.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.ChoiceDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.UnmarshalDefinition;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnmarshallerDefinitionTest {

  RouteDefinition routeDefinition;

  @BeforeEach
  void setUp() {
    routeDefinition = new RouteDefinition();
  }

  @Test
  void WHEN_unmarshallerUsedWithDataFormatDefinition_THEN_unmarshallerIsAddedToRoute() {
    // arrange
    JaxbDataFormat dataFormatDefinition = new JaxbDataFormat();

    // act
    UnmarshallerDefinition marshallerDefinition =
        UnmarshallerDefinition.forDataFormat(dataFormatDefinition);
    marshallerDefinition.accept(routeDefinition);

    // assert
    assertThat(routeDefinition.getOutputs())
        .hasSize(1)
        .hasExactlyElementsOfTypes(ChoiceDefinition.class);
    UnmarshalDefinition processorDefinition =
        (UnmarshalDefinition)
            ((ChoiceDefinition) routeDefinition.getOutputs().get(0))
                .getOtherwise()
                .getOutputs()
                .get(0);
    assertThat(processorDefinition.getDataFormatType())
        .isInstanceOf(dataFormatDefinition.getClass());
  }

  @Test
  void WHEN_unmarshallerUsedWithDataFormat_THEN_unmarshallerIsAddedToRoute() {
    // arrange
    JacksonDataFormat jacksonDataFormat = new JacksonDataFormat();

    // act
    UnmarshallerDefinition marshallerDefinition =
        UnmarshallerDefinition.forDataFormat(jacksonDataFormat);
    marshallerDefinition.accept(routeDefinition);

    // assert
    assertThat(routeDefinition.getOutputs())
        .hasSize(1)
        .hasExactlyElementsOfTypes(ChoiceDefinition.class);
    UnmarshalDefinition processorDefinition =
        (UnmarshalDefinition)
            ((ChoiceDefinition) routeDefinition.getOutputs().get(0))
                .getOtherwise()
                .getOutputs()
                .get(0);
    assertThat(processorDefinition.getDataFormatType().getDataFormat())
        .isInstanceOf(jacksonDataFormat.getClass());
  }
}
