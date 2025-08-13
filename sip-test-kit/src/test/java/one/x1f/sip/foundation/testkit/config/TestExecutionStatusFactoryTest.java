package one.x1f.sip.foundation.testkit.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import one.x1f.sip.foundation.testkit.configurationproperties.TestCaseDefinition;
import one.x1f.sip.foundation.testkit.configurationproperties.models.EndpointProperties;
import one.x1f.sip.foundation.testkit.workflow.TestExecutionStatus;
import org.apache.camel.CamelContext;
import org.apache.camel.ExtendedCamelContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = CamelContext.class)
class TestExecutionStatusFactoryTest {

  public static final String ENDPOINT_ID = "endpoint";

  private TestExecutionStatusFactory subject;
  private TestCaseDefinition testCaseDefinition;

  @BeforeEach
  void setup() {
    CamelContext camelContext = mock(CamelContext.class);
    when(camelContext.getCamelContextExtension()).thenReturn(mock(ExtendedCamelContext.class));
    subject = new TestExecutionStatusFactory(camelContext);
    EndpointProperties whenExecute = new EndpointProperties();
    whenExecute.setEndpointId(ENDPOINT_ID);
    testCaseDefinition = new TestCaseDefinition();
    testCaseDefinition.setTitle("title");
    testCaseDefinition.setWhenExecute(whenExecute);
  }

  @Test
  void When_generateTestReport_With_WhenExecute_Then_ExpectedResponseExists() {
    // arrange
    EndpointProperties thenExpectResponse = new EndpointProperties();
    thenExpectResponse.setEndpointId(ENDPOINT_ID);
    List<EndpointProperties> thenExpect = new ArrayList<>();
    thenExpect.add(thenExpectResponse);
    testCaseDefinition.setThenExpect(thenExpect);

    // act
    TestExecutionStatus testExecutionStatus = subject.generateTestReport(testCaseDefinition);

    // assert
    assertThat(testExecutionStatus.getTestName()).isEqualTo("title");
    assertThat(testExecutionStatus.getAdapterReport().getExpectedResponse()).isNotNull();
  }

  @Test
  void When_generateTestReport_With_missingWhenExecute_Then_emptyExchange() {
    // act
    TestExecutionStatus testExecutionStatus = subject.generateTestReport(testCaseDefinition);

    // assert
    assertThat(testExecutionStatus.getTestName()).isEqualTo("title");
    assertThat(testExecutionStatus.getAdapterReport().getExpectedResponse().getMessage().getBody())
        .isNull();
    assertThat(
            testExecutionStatus.getAdapterReport().getExpectedResponse().getMessage().getHeaders())
        .isEmpty();
  }
}
