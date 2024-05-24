package ${package}.scenarios.models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Residence {
  private Translation city;
  private Translation country;
  private Translation cityNow;
  private Translation countryNow;
  private Translation locationString;
  private Translation continent;
}
