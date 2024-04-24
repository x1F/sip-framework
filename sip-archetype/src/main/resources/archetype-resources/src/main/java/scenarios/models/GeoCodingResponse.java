package ${package}.scenarios.models;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class GeoCodingResponse {
    private BigDecimal generationtime_ms;
    private List<GeoCodingResult> results = new ArrayList<>();
}