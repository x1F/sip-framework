package one.x1f.sip.foundation.core.apps.declarative.mappingadapter;

import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.CommonDomainTypes.ResourceResponse;
import one.x1f.sip.foundation.core.apps.declarative.mappingadapter.FrontEndTypes.UserResponse;
import one.x1f.sip.foundation.core.declarative.model.ModelMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FrontEndSystemResponseMapper extends ModelMapper<ResourceResponse, UserResponse> {

  @Mapping(target = "userId", source = "id")
  @Mapping(target = "username", source = "resourceName")
  @Override
  public UserResponse mapToTargetModel(ResourceResponse sourceModel);

  @Override
  default Class<ResourceResponse> getSourceModelClass() {
    return ResourceResponse.class;
  }

  @Override
  default Class<UserResponse> getTargetModelClass() {
    return UserResponse.class;
  }
}
