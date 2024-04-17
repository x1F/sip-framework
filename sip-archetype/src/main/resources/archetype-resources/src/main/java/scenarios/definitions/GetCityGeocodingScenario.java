package ${package}.scenarios.definitions;

import ${package}.scenarios.models.GeoCodingRequest;
import ${package}.scenarios.models.GeoCodingResponse;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

/**
 * Integration scenario which fetches geocoding data of a city
 */
@IntegrationScenario(
        scenarioId = GetCityGeocodingScenario.ID,
        requestModel = GeoCodingRequest.class,
        responseModel = GeoCodingResponse.class)
public class GetCityGeocodingScenario extends IntegrationScenarioBase {

    public static final String ID = "GetCityGeocodingScenario";
}
