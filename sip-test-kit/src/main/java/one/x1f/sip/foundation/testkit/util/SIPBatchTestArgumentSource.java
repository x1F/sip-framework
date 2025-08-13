package one.x1f.sip.foundation.testkit.util;

import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import one.x1f.sip.foundation.testkit.SIPBatchTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

public class SIPBatchTestArgumentSource implements ArgumentsProvider {

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
      throws Exception {
    SIPBatchTest executedBatchTest = (SIPBatchTest) extensionContext.getRequiredTestInstance();
    return executedBatchTest.getTestCaseCollector().getTestCases().stream()
        .map(testCase -> arguments(named(testCase.getTestName(), testCase)));
  }
}
