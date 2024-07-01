package de.ikor.sip.foundation.core.apps.declarative.mappingadapter;

import de.ikor.sip.foundation.core.apps.declarative.mappingadapter.CommonDomainTypes.ResourceRequest;
import de.ikor.sip.foundation.core.declarative.model.ModelMapper;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

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
