package one.x1f.sip.foundation.core.apps.declarative.mappingadapter;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

public class CommonDomainTypes {

  @Value
  @Builder
  @Jacksonized
  public static class ResourceRequest {
    Integer id;
    String resourceType;
  }

  @Data
  @Builder
  public static class ResourceResponse {
    private Integer id;
    private String resourceName;
    private String resourceType;
  }
}
