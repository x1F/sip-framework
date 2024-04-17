package ${package}.scenarios.definitions;

import ${package}.connectorgroups.${connectorGroup2}.connectors.AirQualityOutboundConnector;
import ${package}.scenarios.models.AirQualityRequest;
import ${package}.scenarios.models.AirQualityResponse;
import ${package}.scenarios.processes.AirQualityProcess;
import de.ikor.sip.foundation.core.declarative.annonation.IntegrationScenario;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.scenario.ScenarioOrchestrator;
import de.ikor.sip.foundation.core.declarative.scenario.IntegrationScenarioBase;

/**
 * Integration scenario which fetches air quality data of a city by its longitude and latitude
 */
@IntegrationScenario(
        scenarioId = GetAirQualityLatLonScenario.ID,
        requestModel = AirQualityRequest.class,
        responseModel = AirQualityResponse.class)
public class GetAirQualityLatLonScenario extends IntegrationScenarioBase {

    public static final String ID = "GetAirQualityLatLonScenario";

    // define scenario orchestation
    @Override
    public Orchestrator<ScenarioOrchestrationInfo> getOrchestrator() {
        return ScenarioOrchestrator.forOrchestrationDslWithResponse(
                AirQualityResponse.class,
                dsl -> {
                    dsl.forScenarioProviders(AirQualityProcess.class)
                            .callOutboundConnector(AirQualityOutboundConnector.class)
                            .andHandleResponse(
                                    (latestResponse, context) ->
                                            latestResponse.setRequestedBy("Process Orchestrator"));
                });
    }
}
