package ${package}.scenarios.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class GeoCodingResult {
    private Integer id;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer elevation;
    private String feature_code;
    private String country_code;
    private Integer admin1_id;
    private Integer admin2_id;
    private Integer admin3_id;
    private Integer admin4_id;
    private String timezone;
    private Integer population;
    private Integer country_id;
    private List postcodes;
    private String country;
    private String admin1;
    private String admin2;
    private String admin3;
    private String admin4;
}
