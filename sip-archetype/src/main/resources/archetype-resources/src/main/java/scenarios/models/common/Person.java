package ${package}.scenarios.models.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Person {
  private Translation knownName;
  private Translation givenName;
  private Translation familyName;
  private Translation fullName;
  private String filename;
  private String penname;
  private String gender;
  private Event birth;
  private Event death;
}
