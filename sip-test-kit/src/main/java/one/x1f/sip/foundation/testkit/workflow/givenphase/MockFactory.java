package one.x1f.sip.foundation.testkit.workflow.givenphase;

import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/** Factory class used for creation of external call mocks. */
@Component
@RequiredArgsConstructor
@Scope(value = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class MockFactory {
  private final Mock mock;

  /**
   * Creates new mock instance and sets required properties.
   *
   * @param testName Name of test case
   * @param returnExchange {@link Exchange}
   * @return creted {@link Mock} instance
   */
  public Mock newMockInstance(String testName, Exchange returnExchange) {
    mock.setTestName(testName);
    mock.setReturnExchange(returnExchange);
    return mock;
  }
}
