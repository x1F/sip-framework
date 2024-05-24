package ${package}.scenarios.definitions;

import ${package}.scenarios.models.NobelPrizeRequest;
import ${package}.scenarios.models.NobelPrizeCommonModel;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

@IntegrationScenario(
    scenarioId = GetNobelPrizeAndLaureateDetails.ID,
    requestModel = NobelPrizeRequest.class,
    responseModel = NobelPrizeCommonModel.class)
public class GetNobelPrizeAndLaureateDetails extends IntegrationScenarioBase {
  public static final String ID = "GetNobelPrizeAndLaureateDetails";
}
