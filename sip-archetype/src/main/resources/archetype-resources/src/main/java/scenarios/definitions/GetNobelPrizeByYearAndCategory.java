package ${package}.scenarios.definitions;

import ${package}.config.SIPAdapterExceptionHandler;
import ${package}.scenarios.models.NobelPrizeRequest;
import ${package}.scenarios.models.nobelprize.NobelPrize;
import one.x1f.sip.foundation.core.declarative.annotation.ConfigurationHandler;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

@IntegrationScenario(
    scenarioId = GetNobelPrizeByYearAndCategory.ID,
    requestModel = NobelPrizeRequest.class,
    responseModel = NobelPrize[].class)
@ConfigurationHandler(SIPAdapterExceptionHandler.class)
public class GetNobelPrizeByYearAndCategory extends IntegrationScenarioBase {
  public static final String ID = "GetNobelPrizeByYearAndCategory";
}
