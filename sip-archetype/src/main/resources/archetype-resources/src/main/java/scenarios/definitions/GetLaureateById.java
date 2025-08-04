package ${package}.scenarios.definitions;

import ${package}.config.SIPAdapterExceptionHandler;
import ${package}.scenarios.models.laureate.Laureate;
import de.ikor.sip.foundation.core.declarative.annotation.ConfigurationHandler;
import de.ikor.sip.foundation.core.declarative.annotation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

@IntegrationScenario(
    scenarioId = GetLaureateById.ID,
    requestModel = Integer.class,
    responseModel = Laureate[].class)
@ConfigurationHandler(SIPAdapterExceptionHandler.class)
public class GetLaureateById extends IntegrationScenarioBase {
  public static final String ID = "GetLaureateById";
}
