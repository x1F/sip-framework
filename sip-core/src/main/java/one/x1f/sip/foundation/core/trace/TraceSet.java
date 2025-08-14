package one.x1f.sip.foundation.core.trace;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Data;

/** Wraps a set of ordered exchange ids used for tracing */
@Data
public class TraceSet implements Serializable {
  private Set<String> exchangeIds = new LinkedHashSet<>();

  public TraceSet() {}

  private TraceSet(Set<String> exchangeIds) {
    this.exchangeIds = exchangeIds;
  }

  /**
   * Clone trace set and add exchangeId to it
   *
   * @param exchangeId id of an exchange
   * @return cloned {@link TraceSet}
   */
  public TraceSet cloneAndAdd(String exchangeId) {
    Set<String> set = new LinkedHashSet<>(exchangeIds);
    TraceSet list = new TraceSet(set);
    list.add(exchangeId);
    return list;
  }

  @Override
  public String toString() {
    return String.join(",", exchangeIds);
  }

  private void add(String exchangeId) {
    if (!exchangeIds.contains(exchangeId)) {
      exchangeIds.add(exchangeId);
    }
  }
}
