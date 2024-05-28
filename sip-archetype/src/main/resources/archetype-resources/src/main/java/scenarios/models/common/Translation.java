package ${package}.scenarios.models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Translation {
  private String en;
  private String se;
  private String no;
  private String latitude;
  private String longitude;
}
