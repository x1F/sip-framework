package ${package}.scenarios.processes;

import ${package}.scenarios.definitions.GetAirQualityByCityScenario;
import ${package}.scenarios.definitions.GetAirQualityLatLonScenario;
import ${package}.scenarios.definitions.GetCityGeocodingScenario;
import ${package}.scenarios.models.AirQualityRequest;
import ${package}.scenarios.models.GeoCodingResponse;
import ${package}.scenarios.models.GeoCodingResult;
import de.ikor.sip.foundation.core.declarative.annonation.CompositeProcess;
import de.ikor.sip.foundation.core.declarative.orchestration.Orchestrator;
import de.ikor.sip.foundation.core.declarative.orchestration.process.CompositeProcessOrchestrationInfo;
import de.ikor.sip.foundation.core.declarative.orchestration.process.ProcessOrchestrator;
import de.ikor.sip.foundation.core.declarative.process.CompositeProcessBase;
import de.ikor.sip.foundation.core.util.exception.SIPAdapterException;
import lombok.extern.slf4j.Slf4j;

/**
 * Process which orchestrates fetching data.
 * It will first fetch geo coding data for a city and after get the air quality based on fetched data
 */
@Slf4j
@CompositeProcess(
        processId = "AirQualityProcessr",
        consumers = {GetCityGeocodingScenario.class, GetAirQualityLatLonScenario.class},
        provider = GetAirQualityByCityScenario.class)
public class AirQualityProcess extends CompositeProcessBase {

    // define process orchestation
    @Override
    public Orchestrator<CompositeProcessOrchestrationInfo> getOrchestrator() {
        return ProcessOrchestrator.forOrchestrationDsl(
                dsl -> {
                    // fetch longitude and latitude of a city
                    dsl.callConsumer(GetCityGeocodingScenario.class)
                            // mark no response handling is needed
                            .withNoResponseHandling()
                            // fetch air quality based on longitude and latitude of a city
                            .callConsumer(GetAirQualityLatLonScenario.class)
                            // prepare request before invoking GetAirQualityLatLonScenario
                            .withRequestPreparation(
                                    context -> {
                                        GeoCodingResponse response =
                                                context.<GeoCodingResponse>getLatestResponse().get();
                                        GeoCodingResult result = response.getResults().stream().findFirst()
                                                .orElseThrow(() ->
                                                        new SIPAdapterException("Invalid value was provided for city"));
                                        return AirQualityRequest.builder()
                                                .lat(result.getLatitude())
                                                .lon(result.getLongitude())
                                                .build();
                                    })
                            // define response handling for GetAirQualityLatLonScenario consumer
                            .withResponseHandling(
                                    (latestResponse, context) -> {
                                        log.debug(String.valueOf(latestResponse));
                                    });
                });
    }
}


