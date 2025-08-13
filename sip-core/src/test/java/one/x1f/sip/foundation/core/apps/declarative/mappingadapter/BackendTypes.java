package one.x1f.sip.foundation.core.apps.declarative.mappingadapter;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.CommonDomainTypes.ResourceRequest;
import one.x1f.sip.foundation.core.declarative.model.ModelMapper;

public class BackendTypes {

  @Value
  @Builder
  @Jacksonized
  public static class BackendResourceRequest {
    Integer id;
    String resourceTypeName;
  }

  public static class BackendRequestModelMapper
      implements ModelMapper<ResourceRequest, BackendResourceRequest> {

    @Override
    public BackendResourceRequest mapToTargetModel(ResourceRequest sourceModel) {
      return BackendResourceRequest.builder()
          .resourceTypeName(sourceModel.getResourceType())
          .id(sourceModel.getId())
          .build();
    }
  }
}
