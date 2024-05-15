package ${package}.scenarios.models.response;

import lombok.Data;

import java.util.List;

@Data
public class NobelPrizeResponse {
  private Integer awardYear;
  private String categoryFullName;
  private String dateAwarded;
  private Integer prizeAmount;
  private Integer prizeAmountAdjusted;
  private List<LaureateInfo> laureates;
}
