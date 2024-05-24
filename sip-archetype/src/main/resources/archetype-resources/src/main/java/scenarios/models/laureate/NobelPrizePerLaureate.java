package ${package}.scenarios.models.laureate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ${package}.scenarios.models.common.Entity;
import ${package}.scenarios.models.common.Residence;
import ${package}.scenarios.models.common.Translation;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NobelPrizePerLaureate {
  private Integer awardYear;
  private Translation category;
  private Translation categoryFullName;
  private String sortOrder;
  private String portion;
  private String dateAwarded;
  private String prizeStatus;
  private Translation motivation;
  private Integer prizeAmount;
  private Integer prizeAmountAdjusted;
  private List<Entity> affiliations;
  private List<Residence> residences;
}
