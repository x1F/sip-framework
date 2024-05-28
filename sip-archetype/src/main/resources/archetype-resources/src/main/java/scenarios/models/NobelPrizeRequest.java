package ${package}.scenarios.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NobelPrizeRequest {
    private String category;
    private String year;
}
