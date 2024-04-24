package ${package}.scenarios.models;

import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
public class AirQualityHourlyData {
    private List<String> time;
    private List<BigDecimal> pm10;
    private List<BigDecimal> pm2_5;
}
