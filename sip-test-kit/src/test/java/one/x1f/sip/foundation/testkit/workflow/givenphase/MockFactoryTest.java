package one.x1f.sip.foundation.testkit.workflow.givenphase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import org.apache.camel.Exchange;
import org.junit.jupiter.api.Test;

class MockFactoryTest {

  private static final String TEST_NAME = "name";

  @Test
  void When_newMockInstance_Expect_MockSet() {
    // arrange
    Mock mockTarget = mock(Mock.class, CALLS_REAL_METHODS);
    MockFactory subject = new MockFactory(mockTarget);
    Exchange exchange = mock(Exchange.class);

    // act
    Mock result = subject.newMockInstance(TEST_NAME, exchange);

    // assert
    assertThat(result.getTestName()).isEqualTo(TEST_NAME);
    assertThat(result.getReturnExchange()).isEqualTo(exchange);
  }
}
