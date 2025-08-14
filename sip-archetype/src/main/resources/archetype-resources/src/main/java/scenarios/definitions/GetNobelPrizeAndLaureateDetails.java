package ${package}.scenarios.definitions;

import ${package}.config.SIPAdapterExceptionHandler;
import ${package}.scenarios.models.NobelPrizeRequest;
import ${package}.scenarios.models.NobelPrizeCommonModel;
import one.x1f.sip.foundation.core.declarative.annotation.ConfigurationHandler;
import one.x1f.sip.foundation.core.declarative.annotation.IntegrationScenario;
import one.x1f.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

@IntegrationScenario(
    scenarioId = GetNobelPrizeAndLaureateDetails.ID,
    requestModel = NobelPrizeRequest.class,
    responseModel = NobelPrizeCommonModel.class)
@ConfigurationHandler(SIPAdapterExceptionHandler.class)
public class GetNobelPrizeAndLaureateDetails extends IntegrationScenarioBase {
  public static final String ID = "GetNobelPrizeAndLaureateDetails";
}
