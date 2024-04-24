package ${package}.scenarios.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirQualityResponse {

    private String requestedBy = "";
    private BigDecimal generationtime_ms;
    private Integer utc_offset_seconds;
    private String timezone;
    private String timezone_abbreviation;

    private AirQualityHourlyUnits hourly_units;

    private AirQualityHourlyData hourly;
}