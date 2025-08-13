package one.x1f.sip.foundation.testkit.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.ConfigurableEnvironment;

@SpringBootTest(
    properties = {"sip.testkit.test-cases-path:test-tcd.yml", "sip.testkit.batch-test:true"},
    classes = ConfigurableEnvironment.class)
class AutoBatchTestCaseLoadingTest {

  private static final String ROUTE_CONTROLLER_SUPERVISE =
      "camel.springboot.routeControllerSuperviseEnabled";

  @Autowired private ConfigurableEnvironment environment;

  @Test
  void GIVEN_necessaryInputProperties_WHEN_prepareTestingEnvironment_THEN_getExpectedProperties() {
    // arrange
    AutoBatchTestCaseLoading subject = new AutoBatchTestCaseLoading();

    // act
    subject.prepareTestingEnvironment(environment);

    // assert
    assertThat(environment.getProperty("test-case-definitions[0].title")).isEqualTo("test");
    assertThat(environment.getProperty(ROUTE_CONTROLLER_SUPERVISE)).isEqualTo("true");
  }
}
