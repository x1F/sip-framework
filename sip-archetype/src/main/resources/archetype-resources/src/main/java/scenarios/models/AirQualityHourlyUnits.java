package ${package}.scenarios.models;

import lombok.Data;

@Data
public class AirQualityHourlyUnits {
    private String time;
    private String pm10;
    private String pm2_5;
}