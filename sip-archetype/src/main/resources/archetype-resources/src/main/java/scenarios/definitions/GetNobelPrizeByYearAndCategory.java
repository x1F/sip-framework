package ${package}.scenarios.definitions;

import ${package}.config.SIPAdapterExceptionHandler;
import ${package}.scenarios.models.NobelPrizeRequest;
import ${package}.scenarios.models.nobelprize.NobelPrize;
import de.ikor.sip.foundation.core.declarative.annonation.ConfigurationHandler;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

@IntegrationScenario(
    scenarioId = GetNobelPrizeByYearAndCategory.ID,
    requestModel = NobelPrizeRequest.class,
    responseModel = NobelPrize[].class)
@ConfigurationHandler(SIPAdapterExceptionHandler.class)
public class GetNobelPrizeByYearAndCategory extends IntegrationScenarioBase {
  public static final String ID = "GetNobelPrizeByYearAndCategory";
}
