package one.x1f.sip.foundation.core.declarative.orchestration.process.dsl;

import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

public class DSLTestHelper {
  public static CallNestedCondition initCallNestedCondition() {
    return new CallNestedCondition(null, null);
  }

  public static CallProcessConsumer initCallProcessConsumer() {
    return new CallProcessConsumer(null, null, TestScenario.class);
  }

  @IntegrationScenario(scenarioId = "ID", requestModel = String.class)
  public class TestScenario extends IntegrationScenarioBase {}
}
