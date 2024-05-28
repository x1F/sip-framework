package ${package}.scenarios.models.nobelprize;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import ${package}.scenarios.models.common.Translation;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NobelPrize {
  private Integer awardYear;
  private Translation category;
  private Translation categoryFullName;
  private String dateAwarded;
  private Integer prizeAmount;
  private Integer prizeAmountAdjusted;
  private Translation topMotivation;
  private List<LaureateBasic> laureates = new ArrayList<>();
}
