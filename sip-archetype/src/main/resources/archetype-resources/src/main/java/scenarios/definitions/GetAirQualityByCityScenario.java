package ${package}.scenarios.definitions;

import ${package}.scenarios.models.AirQualityResponse;
import ${package}.scenarios.models.GeoCodingRequest;
import ${package}.scenarios.processes.AirQualityProcess;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

/**
 * Integration scenario which fetches air quality data of a city by its name
 */
@IntegrationScenario(
        scenarioId = GetAirQualityByCityScenario.ID,
        requestModel = GeoCodingRequest.class,
        responseModel = AirQualityResponse.class)
public class GetAirQualityByCityScenario extends IntegrationScenarioBase {

    public static final String ID = "GetAirQualityByCityScenario";

    // Define scenario orchestation
    // This code block is given only as an example, it can be omitted.
    // n:1 (shown below) or n:n without a response are bound automatically by the framework
    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
        return ScenarioOrchestrator.forOrchestrationDslWithResponse(
                AirQualityResponse.class,
                dsl -> {
                    dsl.forAnyUnspecifiedScenarioProvider()
                            .callScenarioConsumer(AirQualityProcess.class)
                            .andNoResponseHandling();
                });
    }
}
