package ${package}.scenarios.models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Organization {
  private Translation orgName;
  private String nativeName;
  private String acronym;
  private Event founded;
  private Event dissolution;
  private Location headquarters;
}
