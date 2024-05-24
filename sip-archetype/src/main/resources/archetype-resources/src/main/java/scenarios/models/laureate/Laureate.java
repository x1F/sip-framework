package ${package}.scenarios.models.laureate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ${package}.scenarios.models.common.Event;
import ${package}.scenarios.models.common.Location;
import ${package}.scenarios.models.common.Translation;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Laureate {
  private Integer id;
  private Translation knownName;
  private Translation givenName;
  private Translation familyName;
  private Translation fullName;
  private String fileName;
  private String penname;
  private String gender;
  private Event birth;
  private Event death;
  private Translation orgName;
  private String nativeName;
  private String acronym;
  private Event founded;
  private Event dissolution;
  private Location headquarters;
  private Map<String, String> wikipedia;
  private Map<String, String> wikidata;
  private List<String> sameAs;
  private List<NobelPrizePerLaureate> nobelPrizes;
}
