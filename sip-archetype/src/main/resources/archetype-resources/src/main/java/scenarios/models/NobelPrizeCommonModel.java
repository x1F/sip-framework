package ${package}.scenarios.models;

import ${package}.scenarios.models.laureate.Laureate;
import ${package}.scenarios.models.nobelprize.NobelPrize;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NobelPrizeCommonModel {
  private NobelPrize nobelPrize;
  private List<Laureate> laureates = new ArrayList<>();
  private List<Integer> laureatesIds;
}
